package io.github.habatoo.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Конфигурация Spring MVC-приложения.
 *
 * <p>Включает настройки CORS, сканирование компонентов и загрузку свойств профиля.</p>
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "io.github.habatoo")
@PropertySource("classpath:application-${spring.profiles.active:dev}.properties")
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost")  // фронтенд на порту 80
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowedOrigins("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

}
