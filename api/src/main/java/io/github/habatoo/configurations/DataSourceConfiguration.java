package io.github.habatoo.configurations;

import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Конфигурация компонентов Spring для работы с базой данных.
 *
 * <p>Включает настройку источника данных, автоматическую инициализацию миграций через Flyway,
 * настройку пула и создание JdbcTemplate. Все параметры берутся из properties,
 * актуального для выбранного профиля.</p>
 */
@Configuration
@PropertySource("classpath:application-${spring.profiles.active:dev}.properties")
public class DataSourceConfiguration {

    /**
     * Позволяет использовать @Value с параметрами из properties.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /**
     * Настроенный DataSource для подключения к базе данных.
     *
     * @param url               строка подключения
     * @param username          имя пользователя
     * @param password          пароль
     * @param maximumPoolSize   размер пула соединений
     * @param connectionTimeout тайм-аут соединений
     * @return DataSource
     */
    @Bean
    public DataSource dataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${spring.datasource.driver-class-name}") String driverClassName,
            @Value("${spring.datasource.hikari.maximum-pool-size}") int maximumPoolSize,
            @Value("${spring.datasource.hikari.connection-timeout}") int connectionTimeout
    ) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setConnectionTimeout(connectionTimeout);

        return dataSource;
    }

    /**
     * Flyway для автоматического применения миграций.
     *
     * @param dataSource источник данных
     * @param locations  путь к миграциям
     * @param encoding   кодировка миграционных файлов
     * @param version    baseline-версия
     * @param baseline   baselineOnMigrate
     * @param validate   validateOnMigrate
     * @return бин Flyway
     */
    @Bean(initMethod = "migrate")
    public Flyway flyway(
            DataSource dataSource,
            @Value("${spring.flyway.locations}") String locations,
            @Value("${spring.flyway.encoding}") String encoding,
            @Value("${spring.flyway.baseline-version}") String version,
            @Value("${spring.flyway.baseline-on-migrate}") boolean baseline,
            @Value("${spring.flyway.validate-on-migrate}") boolean validate
    ) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .encoding(encoding)
                .baselineVersion(version)
                .baselineOnMigrate(baseline)
                .validateOnMigrate(validate)
                .load();
    }

    /**
     * Компонент JdbcTemplate для выполнения запросов.
     *
     * @param dataSource объект соединения с БД
     * @return бин JdbcTemplate
     */
    @Bean
    @DependsOn("flyway")
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
