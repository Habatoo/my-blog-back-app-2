package io.github.habatoo.configurations;

import io.github.habatoo.repositories.ImageRepository;
import io.github.habatoo.repositories.impl.ImageRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Конфигурация Spring для компонентов, связанных с репозиторием изображений.
 * <p>
 * Описывает бин для ImageRepository — слоя доступа к данным изображений в базе.
 * </p>
 */
@Configuration
public class ImageRepositoryConfiguration {

    /**
     * Создаёт бин репозитория изображений.
     * <p>
     * Репозиторий выполняет весь доступ к данным картинок при помощи JdbcTemplate.
     * </p>
     *
     * @param jdbcTemplate шаблон работы с базой данных
     * @return экземпляр репозитория изображений
     */
    @Bean
    public ImageRepository imageRepository(JdbcTemplate jdbcTemplate) {
        return new ImageRepositoryImpl(jdbcTemplate);
    }
}
