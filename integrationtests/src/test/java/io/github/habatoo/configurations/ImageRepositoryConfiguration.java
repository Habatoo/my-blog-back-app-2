package io.github.habatoo.configurations;

import io.github.habatoo.repositories.ImageRepository;
import io.github.habatoo.repositories.impl.ImageRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

public class ImageRepositoryConfiguration {

    @Bean
    public ImageRepository imageRepository(JdbcTemplate jdbcTemplate) {
        return new ImageRepositoryImpl(jdbcTemplate);
    }
}
