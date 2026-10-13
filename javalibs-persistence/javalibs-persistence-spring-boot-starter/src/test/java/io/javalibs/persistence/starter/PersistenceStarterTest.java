package io.javalibs.persistence.starter;

import io.javalibs.test.PostgresContainerSupport;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Service chỉ khai starter này (không tự thêm module Flyway nào của Boot) vẫn phải
 * khởi động, chạy migration bằng auto-config Flyway của Spring Boot và mang các mặc
 * định của javalibs.
 */
@SpringBootTest(classes = PersistenceStarterTest.App.class)
class PersistenceStarterTest extends PostgresContainerSupport {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void starterAloneRunsFlywayWithJavalibsDefaults() {
        assertThat(flyway.getConfiguration().isCleanDisabled()).isTrue();
        assertThat(flyway.getConfiguration().isValidateMigrationNaming()).isTrue();
        assertThat(jdbc.queryForObject("select count(*) from starter_probe", Integer.class))
                .isZero();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class App {
    }
}
