package io.github.habatoo.controllers;

import io.github.habatoo.configurations.TestDataSourceConfiguration;
import io.github.habatoo.configurations.controllers.CommentControllersConfiguration;
import io.github.habatoo.dto.request.CommentCreateRequestDto;
import io.github.habatoo.dto.request.PostCreateRequestDto;
import io.github.habatoo.handlers.GlobalExceptionHandler;
import io.github.habatoo.service.CommentService;
import io.github.habatoo.service.PostService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты контроллера комментариев через MockMvc.
 * Проверяет, что контроллер возвращает корректные данные для различных операций,
 * при этом данные перед каждым тестом полностью пересоздаются (5 постов, 12 комментариев).
 */
@ExtendWith(SpringExtension.class)
@Testcontainers
@ContextConfiguration(classes = {CommentControllersConfiguration.class, TestDataSourceConfiguration.class})
@DisplayName("Интеграционные тесты CommentController")
class CommentControllerIntegrationTest {

    @Autowired
    CommentController commentController;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    DataSource dataSource;

    @Autowired
    private Flyway flyway;

    MockMvc mockMvc;

    /**
     * Перед каждым тестом база очищается и заполняется
     * 5 тестовыми постами и 12 комментариями,
     * чтобы тесты работали с детерминированными ID и данными.
     */
    @BeforeEach
    @DisplayName("Подготовка тестовых данных для каждого теста")
    void setUp() throws Exception {
        flyway.clean();
        flyway.migrate();

        this.mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        postService.createPost(new PostCreateRequestDto(
                "Мой первый пост о Java",
                "Изучаю Java и Spring Framework. Очень интересная технология!",
                List.of("java", "spring", "programming")
        ));
        postService.createPost(new PostCreateRequestDto(
                "Spring Boot преимущества",
                "Spring Boot упрощает разработку приложений. Автоконфигурация - это круто!",
                List.of("java", "spring")
        ));
        postService.createPost(new PostCreateRequestDto(
                "Работа с базами данных",
                "Рассказываю о основах работы с PostgreSQL и H2 в Spring приложениях. И еще добиваем число символов больше 128 и проверяем разные символы 1234567890!\"№;%:?*()_/{}[]",
                List.of("java", "database", "tutorial")
        ));
        postService.createPost(new PostCreateRequestDto(
                "Советы по программированию",
                "Несколько полезных советов для начинающих разработчиков.",
                List.of("programming", "tutorial")
        ));
        postService.createPost(new PostCreateRequestDto(
                "Без тегов пример",
                "Этот пост создан без тегов для демонстрации.",
                List.of()
        ));

        // 6 уникальных комментариев (идут в базу по времени и id автоинкремент)
        commentService.createComment(new CommentCreateRequestDto(1L, "Отличный первый пост! Удачи в изучении Java!"));
        commentService.createComment(new CommentCreateRequestDto(1L, "Spring Framework действительно мощный инструмент."));
        commentService.createComment(new CommentCreateRequestDto(2L, "Spring Boot экономит так много времени!"));
        commentService.createComment(new CommentCreateRequestDto(2L, "Можно пример настройки автоконфигурации?"));
        commentService.createComment(new CommentCreateRequestDto(2L, "Спасибо за полезную информацию!"));
        commentService.createComment(new CommentCreateRequestDto(3L, "Хорошее объяснение основ работы с БД."));

        // Те же 6 комментариев снова, чтобы итогово для id=1 будет 4, для id=2 будет 6, для id=3 будет 2
        commentService.createComment(new CommentCreateRequestDto(1L, "Отличный первый пост! Удачи в изучении Java!"));
        commentService.createComment(new CommentCreateRequestDto(1L, "Spring Framework действительно мощный инструмент."));
        commentService.createComment(new CommentCreateRequestDto(2L, "Spring Boot экономит так много времени!"));
        commentService.createComment(new CommentCreateRequestDto(2L, "Можно пример настройки автоконфигурации?"));
        commentService.createComment(new CommentCreateRequestDto(2L, "Спасибо за полезную информацию!"));
        commentService.createComment(new CommentCreateRequestDto(3L, "Хорошее объяснение основ работы с БД."));
    }

    /**
     * Проверяет, что по postId=1 возвращаются все комментарии для этого поста.
     * Ожидается 4 комментария, все тексты и ID должны совпадать точно с загруженными.
     */
    @Test
    @DisplayName("Получение списка комментариев по postId=1 (ожидается 4 комментария)")
    void getCommentsByPostId() throws Exception {
        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                          [
                            {"id": 1, "text": "Отличный первый пост! Удачи в изучении Java!", "postId": 1},
                            {"id": 2, "text": "Spring Framework действительно мощный инструмент.", "postId": 1},
                            {"id": 7, "text": "Отличный первый пост! Удачи в изучении Java!", "postId": 1},
                            {"id": 8, "text": "Spring Framework действительно мощный инструмент.", "postId": 1}
                          ]
                        """));
    }

    /**
     * Проверяет получение конкретного комментария по postId=2 и id=3.
     * Ожидается комментарий из заранее загруженных данных.
     */
    @Test
    @DisplayName("Получение комментария по postId=2 и id=3")
    void getCommentByPostIdAndId() throws Exception {
        mockMvc.perform(get("/api/posts/2/comments/3"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                          {
                            "id": 3,
                            "text": "Spring Boot экономит так много времени!",
                            "postId": 2
                          }
                        """));
    }

    /**
     * Проверяет создание комментария к посту 3 и возвращает его.
     * Ожидается корректный id и совпадение текста.
     */
    @Test
    @DisplayName("Создание нового комментария к postId=3")
    void createComment() throws Exception {
        mockMvc.perform(post("/api/posts/3/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {"text": "Проверочный комментарий к посту 3", "postId": 3}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.text").value("Проверочный комментарий к посту 3"))
                .andExpect(jsonPath("$.postId").value(3));
    }

    /**
     * Изменяет существующий комментарий (id=4, postId=2).
     * Ожидается отражение новых данных после обновления.
     */
    @Test
    @DisplayName("Изменение комментария по id=4, postId=2")
    void updateComment() throws Exception {
        mockMvc.perform(put("/api/posts/2/comments/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {"id": 4, "text": "Изменённый комментарий к посту 2", "postId": 2}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                            {"id": 4, "text": "Изменённый комментарий к посту 2", "postId": 2}
                        """));
    }

    /**
     * Удаляет комментарий с id=5, postId=2. Проверяет, что операция успешна.
     */
    @Test
    @DisplayName("Удаление комментария по id=5, postId=2")
    void deleteComment() throws Exception {
        mockMvc.perform(delete("/api/posts/2/comments/5"))
                .andExpect(status().isOk());
    }
}
