package io.github.habatoo.repositories.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * <h2>Тесты инкремента и декремента счетчика комментариев в PostRepositoryImpl</h2>
 *
 */
@DisplayName("Тесты метода incrementCommentsCount/decrementCommentsCount изменения количества комментариев.")
public class PostRepositoryIncrementDecrementCommentsTest extends PostRepositoryTestBase {

    /**
     * Проверяет, что при вызове incrementCommentsCount правильный SQL-запрос
     * выполняется и метод не выбрасывает исключений.
     */
    @Test
    @DisplayName("Должен увеличить счетчик комментариев")
    void shouldIncrementCommentsCountTest() {
        when(jdbcTemplate.update(
                """
                        UPDATE post SET comments_count = comments_count + 1 WHERE id = ?
                        """,
                POST_ID
        )).thenReturn(1);

        assertDoesNotThrow(() -> postRepository.incrementCommentsCount(POST_ID));

        verify(jdbcTemplate).update(
                """
                        UPDATE post SET comments_count = comments_count + 1 WHERE id = ?
                        """,
                POST_ID
        );
    }

    /**
     * Проверяет, что при вызове decrementCommentsCount правильный SQL-запрос
     * выполняется и метод не выбрасывает исключений.
     */
    @Test
    @DisplayName("Должен уменьшить счетчик комментариев")
    void shouldDecrementCommentsCountTest() {
        when(jdbcTemplate.update(
                """
                        UPDATE post
                        SET comments_count = CASE WHEN comments_count > 0 THEN comments_count - 1 ELSE 0 END
                        WHERE id = ?
                        """,
                POST_ID
        )).thenReturn(1);

        assertDoesNotThrow(() -> postRepository.decrementCommentsCount(POST_ID));

        verify(jdbcTemplate).update(
                """
                        UPDATE post
                        SET comments_count = CASE WHEN comments_count > 0 THEN comments_count - 1 ELSE 0 END
                        WHERE id = ?
                        """,
                POST_ID
        );
    }
}
