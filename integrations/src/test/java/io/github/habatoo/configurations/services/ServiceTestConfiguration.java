package io.github.habatoo.configurations.services;

import io.github.habatoo.repositories.CommentRepository;
import io.github.habatoo.repositories.PostRepository;
import io.github.habatoo.service.*;
import io.github.habatoo.service.impl.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application-${spring.profiles.active:test}.properties")
public class ServiceTestConfiguration {

    @Bean
    public PathResolver pathResolver(
            @Value("${app.upload.dir:uploads/posts/}") String uploadDir) {
        return new PathResolverImpl(uploadDir);
    }

    @Bean
    public FileNameGenerator fileNameGenerator(
            @Value("${app.image.default-extension}") String defaultExtension) {
        return new FileNameGeneratorImpl(defaultExtension);
    }

    @Bean
    public FileStorageService fileStorageService(
            @Value("${app.upload.dir:uploads/posts/}") String uploadDir,
            @Value("${app.upload.auto-create-dir:true}") boolean autoCreateDir,
            FileNameGenerator fileNameGenerator,
            PathResolver pathResolver) {
        return new FileStorageServiceImpl(
                uploadDir,
                autoCreateDir,
                fileNameGenerator,
                pathResolver
        );
    }

    @Bean
    public PostService postService(
            PostRepository postRepository,
            FileStorageService fileStorageService) {
        return new PostServiceImpl(postRepository, fileStorageService);
    }

    @Bean
    public CommentService commentService(
            CommentRepository commentRepository,
            PostService postService) {
        return new CommentServiceImpl(commentRepository, postService);
    }
}
