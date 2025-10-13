package io.github.habatoo.repositories;

import io.github.habatoo.configurations.TestDataSourceConfiguration;
import io.github.habatoo.configurations.repositories.CommentRepositoryConfiguration;
import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Класс интеграционных тестов для репозитория комментариев {@link CommentRepository}.
 * <p>
 * Тесты проверяют основные операции с комментариями: создание, поиск, обновление и удаление.
 * Также учитываются варианты с несуществующими данными и проверяется корректность обработки ошибок.
 * </p>
 */
@SpringJUnitConfig(classes = {TestDataSourceConfiguration.class, CommentRepositoryConfiguration.class})
@DisplayName("Интеграционные тесты CommentRepository")
public class CommentRepositoryIntegrationTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Подготавливает чистую тестовую базу и создаёт тестовые записи в таблице постов перед каждым тестом.
     */
    @BeforeEach
    @DisplayName("Подготовка тестовой базы и вставка тестовых постов")
    void setup() {
        flyway.clean();
        flyway.migrate();

        for (long id = 1; id <= 4; id++) {
            jdbcTemplate.update(
                    "INSERT INTO post (id, title, text, likes_count, comments_count, created_at, updated_at) " +
                            "VALUES (?, ?, ?, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                    id,
                    "Тестовый пост " + id,
                    "Содержимое " + id);
        }
    }

    /**
     * Тестирует сохранение нового комментария и поиск всех комментариев по идентификатору поста.
     */
    @Test
    @DisplayName("Сохранение комментария и поиск по postId")
    void testSaveAndFindByPostIdTest() {
        CommentCreateRequest newComment = new CommentCreateRequest(1L, "Тестовый комментарий");
        CommentResponse saved = commentRepository.save(newComment);

        assertThat(saved).isNotNull();
        assertThat(saved.text()).isEqualTo("Тестовый комментарий");

        List<CommentResponse> comments = commentRepository.findByPostId(1L);
        assertThat(comments).isNotEmpty();
        assertThat(comments).extracting("text").contains("Тестовый комментарий");
    }

    /**
     * Проверяет поиск комментария по сочетанию postId и commentId при существующих данных.
     */
    @Test
    @DisplayName("Поиск комментария по postId и commentId (существующий)")
    void testFindByPostIdAndIdExistingTest() {
        CommentCreateRequest newComment = new CommentCreateRequest(2L, "Комментарий для поиска");
        CommentResponse saved = commentRepository.save(newComment);

        Optional<CommentResponse> found = commentRepository.findByPostIdAndId(2L, saved.id());
        assertThat(found).isPresent();
        assertThat(found.get().text()).isEqualTo("Комментарий для поиска");
    }

    /**
     * Проверяет поведение при поиске комментария с несуществующими идентификаторами postId и commentId.
     */
    @Test
    @DisplayName("Поиск комментария по postId и commentId (не существующий)")
    void testFindByPostIdAndIdNonExistingTest() {
        Optional<CommentResponse> found = commentRepository.findByPostIdAndId(99L, 999L);
        assertThat(found).isNotPresent();
    }

    /**
     * Тестирует успешное обновление текста существующего комментария.
     */
    @Test
    @DisplayName("Обновление текста комментария (существующий комментарий)")
    void testUpdateTextExistingTest() {
        CommentCreateRequest newComment = new CommentCreateRequest(3L, "Старый текст");
        CommentResponse saved = commentRepository.save(newComment);

        CommentResponse updated = commentRepository.updateText(saved.id(), "Новый текст");
        assertThat(updated.text()).isEqualTo("Новый текст");
    }

    /**
     * Проверяет, что попытка обновления несуществующего комментария приводит к исключению.
     */
    @Test
    @DisplayName("Обновление текста комментария (не существующий комментарий)")
    void testUpdateTextNonExistingTest() {
        assertThatThrownBy(() -> commentRepository.updateText(999L, "Новый текст"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("не найден для обновления");
    }

    /**
     * Тестирует успешное удаление комментария по id.
     */
    @Test
    @DisplayName("Удаление комментария по id (существующий)")
    void testDeleteByIdExistingTest() {
        CommentCreateRequest newComment = new CommentCreateRequest(4L, "Будет удалён");
        CommentResponse saved = commentRepository.save(newComment);

        int deletedCount = commentRepository.deleteById(saved.id());
        assertThat(deletedCount).isEqualTo(1);

        Optional<CommentResponse> found = commentRepository.findByPostIdAndId(4L, saved.id());
        assertThat(found).isNotPresent();
    }

    /**
     * Проверяет, что попытка удаления несуществующего комментария возвращает 0 удалённых строк.
     */
    @Test
    @DisplayName("Удаление комментария по id (не существующий)")
    void testDeleteByIdNonExistingTest() {
        int deletedCount = commentRepository.deleteById(999L);
        assertThat(deletedCount).isEqualTo(0);
    }

    /**
     * Проверяет, что вставка комментария с несуществующим postId приводит к ошибке целостности данных.
     */
    @Test
    @DisplayName("Сохранение комментария с несуществующим postId вызывает ошибку")
    void testSaveCommentForNonExistingPostShouldThrowExceptionTest() {
        CommentCreateRequest newComment = new CommentCreateRequest(999L, "Неверный пост");
        assertThatThrownBy(() -> commentRepository.save(newComment))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
