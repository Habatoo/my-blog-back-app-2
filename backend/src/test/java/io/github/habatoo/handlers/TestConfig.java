package io.github.habatoo.handlers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Конфигурационный класс для тестового контекста Spring MVC.
 *
 * <p>Объявляет необходимые бины для тестирования.</p>
 *
 * Использует {@link EnableWebMvc} для включения базовой конфигурации Spring MVC,
 * что типично для тестового окружения контроллеров и обработки запросов.
 */
@Configuration
@EnableWebMvc
public class TestConfig {

    /**
     * Создаёт и регистрирует тестовый контроллер {@link TestDummyController}.
     *
     * @return новый экземпляр TestDummyController
     */
    @Bean
    public TestDummyController dummyController() {
        return new TestDummyController();
    }

    /**
     * Создаёт и регистрирует глобальный обработчик исключений {@link GlobalExceptionHandler}.
     *
     * @return новый экземпляр GlobalExceptionHandler
     */
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
