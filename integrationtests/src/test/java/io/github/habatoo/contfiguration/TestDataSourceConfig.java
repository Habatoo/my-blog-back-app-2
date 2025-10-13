package io.github.habatoo.contfiguration;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Slf4j
@Configuration
@PropertySource("classpath:application-${spring.profiles.active:test}.properties")
public class TestDataSourceConfig {

    /**
     * Позволяет использовать @Value с параметрами из properties.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public DataSource dataSource() {
        log.info("Создаём DataSource для H2");
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DEFAULT_NULL_ORDERING=HIGH;NON_KEYWORDS=VALUE");
        // jdbc:h2:mem:testdb;MODE=PostgreSQL;DEFAULT_NULL_ORDERING=HIGH;NON_KEYWORDS=VALUE
        ds.setUsername("sa");
        ds.setPassword("");
        return ds;
    }

//    @Bean
//    public DataSource dataSource() {
//        return new EmbeddedDatabaseBuilder()
//                .setType(EmbeddedDatabaseType.H2)
//                .build();
//    }

//    @Bean(initMethod = "migrate")
//    public Flyway flyway(DataSource dataSource) {
//        log.info("Настраиваем Flyway миграции");
//        return Flyway.configure()
//                .dataSource(dataSource)
//                .encoding("UTF-8")
//                .locations("classpath:db/migrations")
//                .baselineOnMigrate(true)
//                .load();
//    }

    @Bean(initMethod = "migrate")
    public Flyway flyway(
            DataSource dataSource,
            @Value("${spring.flyway.locations}") String locations,
            @Value("${spring.flyway.encoding}") String encoding,
            @Value("${spring.flyway.baseline-version}") String version,
            @Value("${spring.flyway.baseline-on-migrate}") boolean baseline,
            @Value("${spring.flyway.validate-on-migrate}") boolean validate,
            @Value("${spring.flyway.clean-disabled}") boolean clean
    ) {
        log.info("Настраиваем Flyway миграции");
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .encoding(encoding)
                .baselineVersion(version)
                .baselineOnMigrate(baseline)
                .validateOnMigrate(validate)
                .cleanDisabled(clean)
                .load();
    }

    @Bean
    @DependsOn("flyway")
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}

