package io.github.habatoo.configurations;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

/**
 * Тестовая конфигурация источника данных, миграций и JdbcTemplate для интеграционных тестов.
 * <p>
 * Поддерживает настройку профиля, работу с DataSource (обычно H2),
 * автоматическую миграцию схемы Flyway и инициализацию шаблона JDBC.
 * Использует параметры из application-*.properties для гибкой настройки окружения.
 * </p>
 */
@Slf4j
@Configuration
@PropertySource("classpath:application-${spring.profiles.active:test}.properties")
public class TestDataSourceConfiguration {

    /**
     * Конфигуратор для пробрасывания значений из property-файлов
     * в параметры через аннотацию @Value.
     *
     * @return конфигуратор подстановки параметров
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /**
     * Создаёт и запускает тестовый контейнер PostgreSQL на базе образа postgres:17-alpine.
     * Используется исключительно для целей интеграционного тестирования.
     *
     * @return экземпляр управляемого Testcontainers контейнера PostgreSQL
     */
    @Bean
    public static PostgreSQLContainer<?> postgres() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:17-alpine")
                .withDatabaseName("blog_test")
                .withUsername("test")
                .withPassword("test");
        container.start();
        return container;
    }

    /**
     * Создаёт менеджер транзакций Spring для работы с JDBC DataSource.
     *
     * @param dataSource источник данных для транзакций
     * @return менеджер транзакций DataSourceTransactionManager
     */
    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * Создаёт источник данных (DataSource) на основании параметров контейнера PostgreSQL.
     *
     * @param postgres запущенный контейнер PostgreSQL с параметрами доступа
     * @return настроенный источник данных DriverManagerDataSource
     */
    @Bean
    public DataSource dataSource(PostgreSQLContainer<?> postgres) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(postgres.getDriverClassName());
        ds.setUrl(postgres.getJdbcUrl());
        ds.setUsername(postgres.getUsername());
        ds.setPassword(postgres.getPassword());
        return ds;
    }

    /**
     * Конфигурирует и создаёт бин Flyway для миграции схемы БД
     * на основании property-настроек.
     * Автоматически выполняет миграции при инициализации.
     *
     * @param dataSource источник данных для миграций
     * @param locations  локализация скриптов миграций
     * @param encoding   кодировка скриптов
     * @param version    начальная версия схемы
     * @param baseline   режим baselineOnMigrate
     * @param validate   включение валидации на миграциях
     * @param clean      запрет или разрешение чистки схемы
     * @return сконфигурированный бин Flyway
     */
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

    /**
     * Создаёт бин JdbcTemplate для выполнения SQL-запросов.
     * Гарантирует, что миграции будут применены до инициализации шаблона.
     *
     * @param dataSource источник данных
     * @return бин JdbcTemplate
     */
    @Primary
    @Bean
    @DependsOn("flyway")
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
