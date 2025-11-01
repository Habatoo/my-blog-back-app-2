package io.github.habatoo.controllers;

import io.github.habatoo.Application;
import io.github.habatoo.handlers.GlobalExceptionHandler;
import io.github.habatoo.repositories.mapper.CommentRowMapper;
import io.github.habatoo.service.CommentService;
import io.github.habatoo.service.PostService;
import io.github.habatoo.utils.TestDataProvider;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты контроллера комментариев через MockMvc.
 * Проверяет, что контроллер возвращает корректные данные для различных операций,
 * при этом данные перед каждым тестом полностью пересоздаются (5 постов, 12 комментариев).
 */
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
@DisplayName("Интеграционные тесты CommentController")
class CommentControllerIntegrationTest extends TestDataProvider {

    @Autowired
    CommentController commentController;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRowMapper commentRowMapper;

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
    void setUp() {
        flyway.clean();
        flyway.migrate();

        this.commentController = new CommentController(commentService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        preparePostAndComments(postService, commentService);
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
