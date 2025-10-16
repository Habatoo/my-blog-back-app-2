package io.github.habatoo.controllers;

import io.github.habatoo.configurations.TestDataSourceConfiguration;
import io.github.habatoo.configurations.controllers.ImageControllerConfiguration;
import io.github.habatoo.dto.request.PostCreateRequestDto;
import io.github.habatoo.handlers.GlobalExceptionHandler;
import io.github.habatoo.repositories.ImageRepository;
import io.github.habatoo.service.*;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты контроллера изображений ImageController.
 * Проверяет работу API для загрузки и получения изображений постов через MockMvc.
 */
@ExtendWith(SpringExtension.class)
@Testcontainers
@ContextConfiguration(classes = {ImageControllerConfiguration.class, TestDataSourceConfiguration.class})
@DisplayName("Интеграционные тесты ImageController")
class ImageControllerIntegrationTest {

    @Autowired
    ImageController imageController;

    @Autowired
    ImageService imageService;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    ImageValidator imageValidator;

    @Autowired
    ImageContentTypeDetector contentTypeDetector;

    @Autowired
    private PostService postService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    DataSource dataSource;

    @Autowired
    Flyway flyway;

    MockMvc mockMvc;

    /**
     * Перед каждым тестом база очищается и заново создаётся тестовый пост,
     * чтобы гарантировать корректность загрузки и извлечения изображения.
     */
    @BeforeEach
    @DisplayName("Подготовка тестовых постов для ImageController")
    void setUp() {
        flyway.clean();
        flyway.migrate();
        this.mockMvc = MockMvcBuilders.standaloneSetup(imageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        postService.createPost(new PostCreateRequestDto(
                "Пост с тестовым изображением",
                "Контент",
                List.of("image", "test"))
        );
    }

    /**
     * Тест загружает изображение для поста через multipart/form-data
     * и проверяет успешное выполнение запроса.
     */
    @Test
    @DisplayName("Загрузка изображения для postId=1")
    void updatePostImage() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image", "example.jpg", "image/jpeg", "test-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/posts/1/image")
                        .file(multipartFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk());
    }

    /**
     * Тест получает изображение для существующего поста,
     * предварительно загрузив его через updatePostImage().
     */
    @Test
    @DisplayName("Получение изображения для postId=1")
    void getPostImage() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image", "example.jpg", "image/jpeg", "image-byte-content".getBytes()
        );

        mockMvc.perform(multipart("/api/posts/1/image")
                        .file(multipartFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/1/image"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(content().bytes("image-byte-content".getBytes()));
    }

    /**
     * Проверяет ошибку при попытке получить изображение у несуществующего поста.
     */
    @Test
    @DisplayName("Запрос изображения для несуществующего поста (404)")
    void getPostImageNotFound() throws Exception {
        mockMvc.perform(get("/api/posts/999/image"))
                .andExpect(status().isNotFound());
    }

    /**
     * Проверяет ошибку при попытке загрузить пустой файл.
     */
    @Test
    @DisplayName("Загрузка пустого изображения (ошибка)")
    void updatePostImageWithEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image", "empty.jpg", "image/jpeg", new byte[0]
        );

        mockMvc.perform(multipart("/api/posts/1/image")
                        .file(emptyFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isBadRequest());
    }
}
