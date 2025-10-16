package io.github.habatoo.configurations.controllers;

import io.github.habatoo.configurations.repositories.CommentRepositoryConfiguration;
import io.github.habatoo.configurations.repositories.PostRepositoryConfiguration;
import io.github.habatoo.configurations.services.ServiceTestConfiguration;
import io.github.habatoo.controllers.ImageController;
import io.github.habatoo.repositories.ImageRepository;
import io.github.habatoo.repositories.impl.ImageRepositoryImpl;
import io.github.habatoo.service.FileStorageService;
import io.github.habatoo.service.ImageContentTypeDetector;
import io.github.habatoo.service.ImageService;
import io.github.habatoo.service.ImageValidator;
import io.github.habatoo.service.impl.ImageContentTypeDetectorImpl;
import io.github.habatoo.service.impl.ImageServiceImpl;
import io.github.habatoo.service.impl.ImageValidatorImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Import({
        ServiceTestConfiguration.class,
        PostRepositoryConfiguration.class,
        CommentRepositoryConfiguration.class
})
public class ImageControllerConfiguration {

    @Bean
    public ImageController imageController(ImageService imageService) {
        return new ImageController(imageService);
    }

    @Bean
    public ImageValidator imageValidator() {
        return new ImageValidatorImpl();
    }

    @Bean
    public ImageContentTypeDetector contentTypeDetector() {
        return new ImageContentTypeDetectorImpl();
    }

    @Bean
    public ImageService imageService(
            ImageRepository imageRepository,
            FileStorageService fileStorageService,
            ImageValidator imageValidator,
            ImageContentTypeDetector contentTypeDetector) {
        return new ImageServiceImpl(
                imageRepository,
                fileStorageService,
                imageValidator,
                contentTypeDetector);
    }

    @Bean
    public ImageRepository imageRepository(JdbcTemplate jdbcTemplate) {
        return new ImageRepositoryImpl(jdbcTemplate);
    }
}
