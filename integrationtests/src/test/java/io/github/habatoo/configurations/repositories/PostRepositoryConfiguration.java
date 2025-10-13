package io.github.habatoo.configurations.repositories;

import io.github.habatoo.repositories.PostRepository;
import io.github.habatoo.repositories.impl.PostRepositoryImpl;
import io.github.habatoo.repositories.mapper.PostListRowMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Конфигурация Spring для компонентов репозитория постов.
 * <p>
 * Описывает бины для PostListRowMapper (маппер списка постов)
 * и PostRepository — основного слоя доступа к данным постов блога.
 * </p>
 */
@Configuration
public class PostRepositoryConfiguration {

    /**
     * Создаёт бин маппер списка постов для преобразования
     * строк результата SQL-запросов в DTO PostResponse.
     *
     * @return экземпляр PostListRowMapper
     */
    @Bean
    public PostListRowMapper postListRowMapper() {
        return new PostListRowMapper();
    }

    /**
     * Создаёт бин репозитория постов.
     * <p>
     * Репозиторий обеспечивает CRUD-операции с постами блога,
     * используя JdbcTemplate и PostListRowMapper.
     * </p>
     *
     * @param jdbcTemplate      шаблон работы с базой данных
     * @param postListRowMapper маппер строк в PostResponse
     * @return экземпляр репозитория постов
     */
    @Bean
    public PostRepository postRepository(
            JdbcTemplate jdbcTemplate,
            PostListRowMapper postListRowMapper) {
        return new PostRepositoryImpl(jdbcTemplate, postListRowMapper);
    }
}
