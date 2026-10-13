package io.javalibs.persistence.autoconfigure;

import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class JavalibsFlywayAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JavalibsFlywayAutoConfiguration.class));

    @Test
    void appliesProductionSafeDefaults() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(FlywayConfigurationCustomizer.class);
            FluentConfiguration configuration = new FluentConfiguration();
            context.getBean(FlywayConfigurationCustomizer.class).customize(configuration);

            assertThat(configuration.isCleanDisabled()).isTrue();
            assertThat(configuration.isValidateMigrationNaming()).isTrue();
            assertThat(configuration.isOutOfOrder()).isFalse();
            assertThat(configuration.isBaselineOnMigrate()).isFalse();
        });
    }

    @Test
    void propertiesOverrideDefaults() {
        runner.withPropertyValues(
                        "javalibs.persistence.flyway.baseline-on-migrate=true",
                        "javalibs.persistence.flyway.clean-disabled=false")
                .run(context -> {
                    FluentConfiguration configuration = new FluentConfiguration();
                    context.getBean(FlywayConfigurationCustomizer.class).customize(configuration);
                    assertThat(configuration.isBaselineOnMigrate()).isTrue();
                    assertThat(configuration.isCleanDisabled()).isFalse();
                });
    }

    @Test
    void backsOffWhenSpringBootFlywayModuleIsAbsent() {
        // Boot 4 tách auto-config Flyway sang spring-boot-flyway; thiếu module đó thì
        // không được làm hỏng context, chỉ lùi lại.
        runner.withClassLoader(new FilteredClassLoader(FlywayConfigurationCustomizer.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean("javalibsFlywayDefaultsCustomizer");
                });
    }

    @Test
    void disabledFlagBacksOff() {
        runner.withPropertyValues("javalibs.persistence.flyway.enabled=false")
                .run(context ->
                        assertThat(context).doesNotHaveBean(FlywayConfigurationCustomizer.class));
    }
}
