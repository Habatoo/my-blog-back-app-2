package io.github.habatoo.configurations;

import io.github.habatoo.repositories.PostRepository;
import io.github.habatoo.repositories.impl.PostRepositoryImpl;
import io.github.habatoo.repositories.mapper.PostListRowMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PostRepositoryConfiguration {

    @Bean
    public PostListRowMapper postListRowMapper() {
        return new PostListRowMapper();
    }

    @Bean
    public PostRepository postRepository(
            JdbcTemplate jdbcTemplate,
            PostListRowMapper postListRowMapper) {
        return new PostRepositoryImpl(jdbcTemplate, postListRowMapper);
    }
}
