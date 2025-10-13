package io.github.habatoo.configurations.repositories;

import io.github.habatoo.repositories.CommentRepository;
import io.github.habatoo.repositories.impl.CommentRepositoryImpl;
import io.github.habatoo.repositories.mapper.CommentRowMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Конфигурация Spring для сборки компонентов репозитория комментариев.
 * <p>
 * Описывает бины для CommentRowMapper (маппера строк из БД в DTO)
 * и CommentRepository — основного слоя доступа к данным комментариев.
 * </p>
 */
@Configuration
public class CommentRepositoryConfiguration {

    /**
     * Создаёт бин маппер комментариев для преобразования
     * строк результата SQL-запросов в DTO CommentResponse.
     *
     * @return экземпляр CommentRowMapper
     */
    @Bean
    public CommentRowMapper commentRowMapper() {
        return new CommentRowMapper();
    }

    /**
     * Создаёт бин репозитория для работы с комментариями блога.
     * <p>
     * Использует JdbcTemplate и CommentRowMapper для операций чтения/записи.
     * </p>
     *
     * @param jdbcTemplate     шаблон работы с базой данных
     * @param commentRowMapper маппер строк в CommentResponse
     * @return экземпляр репозитория комментариев
     */
    @Bean
    public CommentRepository commentRepository(
            JdbcTemplate jdbcTemplate,
            CommentRowMapper commentRowMapper) {
        return new CommentRepositoryImpl(jdbcTemplate, commentRowMapper);
    }
}
