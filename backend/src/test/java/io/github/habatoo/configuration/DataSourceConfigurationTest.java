package io.github.habatoo.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тест для конфигурации DataSourceConfiguration.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DataSourceConfiguration.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.maximum-pool-size=10",
        "spring.datasource.connection-timeout=30000",
        "spring.flyway.locations=classpath:db/migration",
        "spring.flyway.encoding=UTF-8",
        "spring.flyway.baseline-version=1",
        "spring.flyway.baseline-on-migrate=true",
        "spring.flyway.validate-on-migrate=false"
})
class DataSourceConfigurationTest {

    @Autowired
    PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer;

    @Autowired
    DataSource dataSource;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    Flyway flyway;

    @Autowired
    ConfigurableEnvironment environment;

    /**
     * <p>Проверяет создание и настройки основных бинов при наличии необходимых properties.</p>
     */
    @Test
    @DisplayName("Все бины DataSourceConfiguration должны быть созданы")
    void allBeansCreatedTest() {
        assertThat(propertySourcesPlaceholderConfigurer).isNotNull();
        assertThat(dataSource).isNotNull();
        assertThat(jdbcTemplate).isNotNull();
        assertThat(flyway).isNotNull();
        assertThat(dataSource).isInstanceOf(HikariDataSource.class);

        HikariDataSource hds = (HikariDataSource) dataSource;
        assertThat(hds.getMaximumPoolSize()).isEqualTo(10);
        assertThat(hds.getConnectionTimeout()).isEqualTo(30000);
    }
}
