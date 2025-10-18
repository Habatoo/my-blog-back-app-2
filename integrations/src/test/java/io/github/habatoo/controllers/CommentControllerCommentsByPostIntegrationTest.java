package io.github.habatoo.controllers;

import io.github.habatoo.configurations.TestDataSourceConfiguration;
import io.github.habatoo.configurations.controllers.CommentControllersConfiguration;
import io.github.habatoo.handlers.GlobalExceptionHandler;
import io.github.habatoo.repositories.mapper.CommentRowMapper;
import io.github.habatoo.service.CommentService;
import io.github.habatoo.service.PostService;
import io.github.habatoo.utils.TestDataProvider;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты контроллера комментариев через MockMvc.
 * Проверяет, что контроллер возвращает корректные данные для различных операций,
 * при этом данные перед каждым тестом полностью пересоздаются.
 */
@ExtendWith(SpringExtension.class)
@Testcontainers
@ContextConfiguration(classes = {CommentControllersConfiguration.class, TestDataSourceConfiguration.class})
@DisplayName("Интеграционные тесты CommentController")
class CommentControllerCommentsByPostIntegrationTest extends TestDataProvider {

    @Autowired
    CommentController commentController;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CommentRowMapper commentRowMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    private MockMvc mockMvc;

    @BeforeEach
    @DisplayName("Подготовка тестовых данных для каждого теста")
    void setUp() {
        this.commentController = new CommentController(commentService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Проверяет, что по postId=1 возвращаются все комментарии для этого поста.
     * Ожидается 2 комментария, все тексты и ID должны совпадать точно с загруженными.
     */
    @Test
    @DisplayName("Получение списка комментариев по postId=1 (ожидается 2 комментария)")
    void getNonZeroCommentsByPostIdTest() throws Exception {
        flyway.clean();
        flyway.migrate();
        preparePostAndComments(postService, commentService);

        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                          [
                            {"id": 1, "text": "Отличный первый пост! Удачи в изучении Java!", "postId": 1},
                            {"id": 2, "text": "Spring Framework действительно мощный инструмент.", "postId": 1}
                          ]
                        """));
    }
}
