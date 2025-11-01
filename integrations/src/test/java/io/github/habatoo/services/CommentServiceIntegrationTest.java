package io.github.habatoo.services;

import io.github.habatoo.Application;
import io.github.habatoo.dto.request.CommentCreateRequestDto;
import io.github.habatoo.dto.request.CommentRequestDto;
import io.github.habatoo.dto.response.CommentResponseDto;
import io.github.habatoo.service.CommentService;
import io.github.habatoo.service.PostService;
import io.github.habatoo.utils.TestDataProvider;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Интеграционные тесты CommentServiceImpl.
 * <p>
 * Покрывают основные бизнес-сценарии работы с комментариями:
 * создание, получение, обновление, удаление и корректное управление счетчиками.
 */
@Transactional
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
@DisplayName("Интеграционные тесты CommentServiceImpl")
class CommentServiceIntegrationTest extends TestDataProvider {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void setUp() {
        flyway.clean();
        flyway.migrate();
        preparePosts(postService);
    }

    /**
     * Тест создания комментария и последующего получения списка комментариев по посту.
     * <p>
     * Проверяется, что созданный комментарий имеет положительный ID,
     * текст совпадает с ожидаемым, и при получении комментариев по посту
     * этот комментарий присутствует в списке.
     */
    @Test
    @DisplayName("Создание комментария через сервис и получение списка комментариев по посту")
    void testCreateAndGetCommentsTest() {
        Long postId = 1L;
        CommentCreateRequestDto req = new CommentCreateRequestDto(postId, "Новый комментарий");
        CommentResponseDto saved = commentService.createComment(req);

        assertThat(saved.id()).isPositive();
        assertThat(saved.text()).isEqualTo("Новый комментарий");
        List<CommentResponseDto> comments = commentService.getCommentsByPostId(postId);

        assertThat(comments).isNotEmpty();
        assertThat(comments).anyMatch(c -> c.text().equals("Новый комментарий"));
    }

    /**
     * Тест обновления текста комментария и получения обновлённого комментария по посту.
     * <p>
     * Создаётся комментарий, затем обновляется его текст.
     * Проверяется, что обновлённый комментарий имеет корректный ID и обновлённый текст,
     * и список комментариев поста содержит обновлённый текст.
     */
    @Test
    @DisplayName("Обновление комментария через сервис и проверка обновлённого текста")
    void testUpdateAndGetCommentsTest() {
        CommentCreateRequestDto req = new CommentCreateRequestDto(1L, "Новый комментарий");
        CommentResponseDto saved = commentService.createComment(req);
        CommentRequestDto reqUpdated = new CommentRequestDto(saved.id(), "Обновленный комментарий", saved.postId());
        CommentResponseDto edited = commentService.updateComment(reqUpdated);

        assertThat(edited.id()).isPositive();
        assertThat(edited.text()).isEqualTo("Обновленный комментарий");
        List<CommentResponseDto> updatedComments = commentService.getCommentsByPostId(1L);

        assertThat(updatedComments).isNotEmpty();
        assertThat(updatedComments).anyMatch(c -> c.text().equals("Обновленный комментарий"));
    }

    /**
     * Тест удаления комментария и проверки уменьшения счетчика комментариев у поста,
     * а также отсутствия удалённого комментария в списке комментариев.
     */
    @Test
    @DisplayName("Удаление комментария уменьшает счётчик комментариев у поста и удаляет комментарий")
    void testDeleteCommentTest() {
        CommentCreateRequestDto req = new CommentCreateRequestDto(1L, "Удаляемый комментарий");
        CommentResponseDto saved = commentService.createComment(req);

        Integer before = jdbcTemplate.queryForObject("SELECT comments_count FROM post WHERE id = ?", Integer.class, 1L);
        commentService.deleteComment(1L, saved.id());
        Integer after = jdbcTemplate.queryForObject("SELECT comments_count FROM post WHERE id = ?", Integer.class, 1L);

        assertThat(after).isEqualTo(before - 1);
        assertThat(commentService.getCommentsByPostId(1L)).allMatch(c -> !c.id().equals(saved.id()));
    }

    /**
     * Тестирование поведения при попытке получить комментарии несуществующего поста.
     * <p>
     * Проверяет, что при отсутствии комментариев возвращается пустой список,
     * а не выбрасывается исключение.
     */
    @Test
    @DisplayName("При отсутствии комментариев для поста возвращается пустой список")
    void testGetCommentsNonExistingPostTest() {
        Long nonExistingPostId = 999L;
        List<CommentResponseDto> result = commentService.getCommentsByPostId(nonExistingPostId);
        assertNotNull(result, "Результат не должен быть null");
        assertTrue(result.isEmpty(), "Список должен быть пустым, если комментарии не найдены");
    }

    /**
     * Тестирование поведения при попытке обновить комментарий несуществующего поста.
     * <p>
     * Проверяется, что вызывается исключение IllegalStateException с сообщением о несуществующем посте.
     */
    @Test
    @DisplayName("При попытке обновить комментарий несуществующего поста выбрасывается исключение")
    void testUpdateCommentNonExistingPostTest() {
        Long nonExistingPostId = 999L;
        assertThatThrownBy(() -> commentService.updateComment(new CommentRequestDto(1L, "text", nonExistingPostId)))
                .isInstanceOf(EmptyResultDataAccessException.class)
                .hasMessageContaining("Комментарий не найден");
    }

    /**
     * Тестирование поведения при попытке обновить несуществующий комментарий.
     * <p>
     * Проверяется, что вызывается исключение IllegalStateException с соответствующим сообщением.
     */
    @Test
    @DisplayName("При попытке обновить несуществующий комментарий выбрасывается исключение")
    void testUpdateNonExistingCommentTest() {
        Long postId = 1L;
        CommentCreateRequestDto req = new CommentCreateRequestDto(postId, "Комментарий");
        commentService.createComment(req);
        Long nonExistingCommentId = 999L;
        CommentRequestDto reqUpdated = new CommentRequestDto(nonExistingCommentId, "Обновление текста", nonExistingCommentId);
        assertThatThrownBy(() -> commentService.updateComment(reqUpdated))
                .isInstanceOf(EmptyResultDataAccessException.class)
                .hasMessageContaining("Комментарий не найден");
    }

    /**
     * Тестирование поведения при попытке удалить комментарий несуществующего поста.
     * <p>
     * Проверяется, что вызывается исключение IllegalStateException с сообщением о несуществующем посте.
     */
    @Test
    @DisplayName("При попытке удалить комментарий несуществующего поста выбрасывается исключение")
    void testDeleteCommentNonExistingPostTest() {
        Long nonExistingPostId = 999L;
        assertThatThrownBy(() -> commentService.deleteComment(nonExistingPostId, 1L))
                .isInstanceOf(EmptyResultDataAccessException.class)
                .hasMessageContaining("Комментарий не найден");
    }

    /**
     * Тестирование поведения при попытке удалить несуществующий комментарий.
     * <p>
     * Проверяется, что вызывается исключение EmptyResultDataAccessException с сообщением о несуществующем комментарии.
     */
    @Test
    @DisplayName("При попытке удалить несуществующий комментарий выбрасывается исключение")
    void testDeleteNonExistingCommentTest() {
        Long postId = 1L;
        CommentCreateRequestDto req = new CommentCreateRequestDto(postId, "Комментарий");
        CommentResponseDto saved = commentService.createComment(req);
        Long nonExistingCommentId = 999L;
        assertThatThrownBy(() -> commentService.deleteComment(postId, nonExistingCommentId))
                .isInstanceOf(EmptyResultDataAccessException.class)
                .hasMessageContaining("Комментарий не найден");
    }
}

