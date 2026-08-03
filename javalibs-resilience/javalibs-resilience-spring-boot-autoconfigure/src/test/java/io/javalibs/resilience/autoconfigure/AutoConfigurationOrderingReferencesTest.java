package io.javalibs.resilience.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code beforeName}/{@code afterName} on {@code @AutoConfiguration} are plain strings: Spring
 * resolves them reflectively while sorting auto-configuration classes and <b>silently drops any
 * name it cannot load</b> — no compile error, no startup failure, no log line. A dependency
 * upgrade that relocates the referenced class therefore breaks the ordering constraint with zero
 * signal; the symptom only shows up as a subtly wrong bean back-off decision in a real
 * application. Bậc 9 hit exactly this: {@link ResilienceAutoConfiguration}'s {@code afterName}
 * pointed at {@code io.github.resilience4j.springboot3.*.autoconfigure.*}
 * (resilience4j-spring-boot3, the Boot 3 annotation-driven artifact). Boot 4's replacement
 * artifact, {@code resilience4j-spring-boot4} 2.4.0, does <b>not</b> use a {@code springboot4}
 * package as the naming pattern would suggest — it renamed the package to
 * {@code io.github.resilience4j.springboot.*} (see {@code CHANGELOG-2.0.md}).
 *
 * <p>This test derives the list of classes to check from this module's own production
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports} —
 * never a class list typed into the test — then reads {@code beforeName()}/{@code afterName()}
 * straight off the real {@code @AutoConfiguration} annotation via reflection and asserts that
 * every {@code org.springframework.boot.*} or {@code io.github.resilience4j.*} reference resolves
 * on this module's test classpath (the resilience4j-spring-boot4 artifact is a test-only
 * dependency added specifically so this assertion is meaningful — see this module's pom.xml).</p>
 */
class AutoConfigurationOrderingReferencesTest {

    private static final String IMPORTS_RESOURCE =
            "/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";

    @Test
    void everyOrderingReferenceResolvesOnTheClasspath() throws Exception {
        List<String> unresolved = new ArrayList<>();
        for (Class<?> autoConfigClass : declaredAutoConfigurationClasses()) {
            AutoConfiguration annotation = autoConfigClass.getAnnotation(AutoConfiguration.class);
            assertThat(annotation)
                    .as("%s is listed in AutoConfiguration.imports but not annotated with "
                            + "@AutoConfiguration", autoConfigClass)
                    .isNotNull();

            for (String fqcn : concat(annotation.beforeName(), annotation.afterName())) {
                if (!fqcn.startsWith("org.springframework.boot.")
                        && !fqcn.startsWith("io.github.resilience4j.")) {
                    // Sibling javalibs module referenced as an optional forward integration —
                    // intentionally allowed to be absent from this module's classpath.
                    continue;
                }
                try {
                    Class.forName(fqcn, false, getClass().getClassLoader());
                } catch (ClassNotFoundException e) {
                    unresolved.add(autoConfigClass.getSimpleName() + " -> \"" + fqcn + "\"");
                }
            }
        }

        assertThat(unresolved)
                .as("beforeName()/afterName() references that no longer resolve on the "
                        + "classpath. The referenced class was most likely relocated by a "
                        + "dependency version bump — find its new fully-qualified name (e.g. via "
                        + "`javap -v` or `unzip -l` on the real jar) and update the string. Do "
                        + "NOT just delete the reference: it exists to make our bean win the "
                        + "@ConditionalOnMissingBean race against the third-party fallback.")
                .isEmpty();
    }

    private static List<Class<?>> declaredAutoConfigurationClasses() throws IOException {
        List<Class<?>> classes = new ArrayList<>();
        try (InputStream in = AutoConfigurationOrderingReferencesTest.class
                .getResourceAsStream(IMPORTS_RESOURCE)) {
            assertThat(in).as("%s must exist on the classpath", IMPORTS_RESOURCE).isNotNull();
            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.strip();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }
                    try {
                        classes.add(Class.forName(trimmed));
                    } catch (ClassNotFoundException e) {
                        throw new AssertionError(
                                "Class listed in AutoConfiguration.imports does not exist: "
                                        + trimmed, e);
                    }
                }
            }
        }
        assertThat(classes).as("AutoConfiguration.imports must not be empty").isNotEmpty();
        return classes;
    }

    private static String[] concat(String[] first, String[] second) {
        String[] result = new String[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
