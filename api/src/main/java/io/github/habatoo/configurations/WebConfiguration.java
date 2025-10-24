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

    private final CorsProperties corsProperties;

    public WebConfiguration(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(corsProperties.pathPattern())
                .allowedOriginPatterns(corsProperties.allowedOriginPatterns())
                .allowedMethods(corsProperties.allowedMethods().toArray(new String[0]))
                .allowedHeaders(corsProperties.allowedHeaders())
                .allowCredentials(corsProperties.allowCredentials())
                .maxAge(corsProperties.maxAge());
    }
}
