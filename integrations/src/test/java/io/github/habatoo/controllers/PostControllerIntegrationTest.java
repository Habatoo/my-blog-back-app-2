package io.github.habatoo.controllers;

import io.github.habatoo.Application;
import io.github.habatoo.handlers.GlobalExceptionHandler;
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
 * Интеграционные тесты контроллера постов (PostController).
 * Проверяет корректность работы CRUD-операций, поиска, пагинации и лайков в REST API.
 * <p>
 * В каждом тесте база инициализируется одними и теми же постами,
 * чтобы результаты запросов можно было проверять однозначно.
 */
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
@DisplayName("Интеграционные тесты PostController")
class PostControllerIntegrationTest extends TestDataProvider {

    @Autowired
    PostController postController;

    @Autowired
    private PostService postService;

    @Autowired
    CommentService commentService;

    @Autowired
    private Flyway flyway;

    MockMvc mockMvc;

    @BeforeEach
    @DisplayName("Подготовка тестовых постов и очистка базы")
    void setUp() {
        flyway.clean();
        flyway.migrate();
        this.mockMvc = MockMvcBuilders.standaloneSetup(postController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        preparePostAndComments(postService, commentService);
    }

    /**
     * Проверяет создание поста через POST-запрос.
     * Ожидается успешное создание и возврат нового id.
     */
    @Test
    @DisplayName("Создание нового поста")
    void createPost() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {
                                   "title": "Test Post",
                                   "text": "Тестовое содержимое.",
                                   "tags": ["test", "example"]
                                 }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Test Post"))
                .andExpect(jsonPath("$.tags.length()").value(2));
    }

    /**
     * Проверяет получение поста по ID.
     * Ожидается корректный возврат для существующего поста.
     */
    @Test
    @DisplayName("Получение поста по id=1")
    void getPostById() throws Exception {
        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Мой первый пост о Java"));
    }

    /**
     * Проверяет получение постов с поиском по тексту и пагинацией.
     * Ожидается 3 найденных поста по заданному поисковому слову,
     * к отображению 2.
     */
    @Test
    @DisplayName("Пагинация и поиск постов")
    void getPostsWithPaginationAndSearch() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("search", "Spring")
                        .param("pageNumber", "1")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(2))
                .andExpect(jsonPath("$.hasPrev").value(false))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.lastPage").value(2));
    }

    /**
     * Проверяет обновление поста по id.
     * Ожидается, что title и text будут обновлены.
     */
    @Test
    @DisplayName("Обновление поста по id=2")
    void updatePost() throws Exception {
        mockMvc.perform(put("/api/posts/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 2,
                                  "title": "Обновленный заголовок",
                                  "text": "Новый текст",
                                  "tags": ["spring", "updated"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("Обновленный заголовок"))
                .andExpect(jsonPath("$.text").value("Новый текст"))
                .andExpect(jsonPath("$.tags.length()").value(2));
    }

    /**
     * Проверяет удаление поста. После этого запрос к этому id вернёт 404.
     */
    @Test
    @DisplayName("Удаление поста по id=3")
    void deletePost() throws Exception {
        mockMvc.perform(delete("/api/posts/3"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/3"))
                .andExpect(status().isNotFound());
    }

    /**
     * Проверяет увеличение лайков для поста.
     * Ожидается увеличение на 1.
     */
    @Test
    @DisplayName("Инкремент лайков для поста id=1")
    void incrementLikes() throws Exception {
        mockMvc.perform(post("/api/posts/1/likes"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        mockMvc.perform(post("/api/posts/1/likes"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }
}
