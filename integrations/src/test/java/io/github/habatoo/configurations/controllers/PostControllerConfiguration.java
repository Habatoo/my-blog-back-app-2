package io.github.habatoo.configurations.controllers;

import io.github.habatoo.configurations.repositories.CommentRepositoryConfiguration;
import io.github.habatoo.configurations.repositories.PostRepositoryConfiguration;
import io.github.habatoo.configurations.services.ServiceTestConfiguration;
import io.github.habatoo.controllers.PostController;
import io.github.habatoo.service.PostService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        ServiceTestConfiguration.class,
        PostRepositoryConfiguration.class,
        CommentRepositoryConfiguration.class
})
public class PostControllerConfiguration {

    @Bean
    public PostController commentController(PostService postService) {
        return new PostController(postService);
    }
}
