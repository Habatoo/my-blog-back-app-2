package io.github.habatoo.contfiguration;

import io.github.habatoo.repository.CommentRepository;
import io.github.habatoo.repository.impl.CommentRepositoryImpl;
import io.github.habatoo.repository.mapper.CommentRowMapper;
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
