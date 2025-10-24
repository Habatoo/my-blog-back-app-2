package io.github.habatoo.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Конфигурация Spring MVC-приложения.
 *
 * <p>Включает настройки CORS, сканирование компонентов и загрузку свойств профиля.</p>
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final Properties properties;

    public WebConfiguration(Properties properties) {
        this.properties = properties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(properties.pathPattern())
                .allowedOriginPatterns(properties.allowedOriginPatterns())
                .allowedMethods(properties.allowedMethods().toArray(new String[0]))
                .allowedHeaders(properties.allowedHeaders())
                .allowCredentials(properties.allowCredentials())
                .maxAge(properties.maxAge());
    }
}
