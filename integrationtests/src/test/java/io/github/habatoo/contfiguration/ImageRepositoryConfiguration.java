package io.github.habatoo.contfiguration;

import io.github.habatoo.repository.ImageRepository;
import io.github.habatoo.repository.impl.ImageRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

public class ImageRepositoryConfiguration {

    @Bean
    public ImageRepository imageRepository(JdbcTemplate jdbcTemplate) {
        return new ImageRepositoryImpl(jdbcTemplate);
    }
}
