package io.github.habatoo.configurations;

import io.github.habatoo.repositories.CommentRepository;
import io.github.habatoo.repositories.impl.CommentRepositoryImpl;
import io.github.habatoo.repositories.mapper.CommentRowMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class CommentRepositoryConfiguration {

    @Bean
    public CommentRowMapper commentRowMapper() {
        return new CommentRowMapper();
    }

    @Bean
    public CommentRepository commentRepository(
            JdbcTemplate jdbcTemplate,
            CommentRowMapper commentRowMapper) {
        return new CommentRepositoryImpl(jdbcTemplate, commentRowMapper);
    }
}
