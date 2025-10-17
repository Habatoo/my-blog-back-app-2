package io.github.habatoo.repositories.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.habatoo.repositories.sql.PostSqlQueries.INCREMENT_LIKES;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * <h2>Тесты метода incrementLikes в PostRepositoryImpl</h2>
 *
 */
@DisplayName("Тесты метода incrementLikes изменения количества лайков.")
public class PostRepositoryIncrementLikesTest extends PostRepositoryTestBase {

    /**
     * Проверяет, что при вызове incrementLikes происходит успешное увеличение количества лайков —
     * вызывается нужный SQL и метод не выбрасывает исключения.
     */
    @Test
    @DisplayName("Должен успешно увеличить счетчик лайков")
    void shouldIncrementLikesTest() {
        when(jdbcTemplate.update(INCREMENT_LIKES, POST_ID)).thenReturn(1);

        assertDoesNotThrow(() -> postRepository.incrementLikes(POST_ID));

        verify(jdbcTemplate).update(INCREMENT_LIKES, POST_ID);
    }

    /**
     * Проверяет, что если при попытке увеличить лайки не найден ни один пост (update=0),
     * метод выбрасывает IllegalStateException с ожидаемым сообщением.
     */
    @Test
    @DisplayName("Должен выбросить EmptyResultDataAccessException если пост не найден при увеличении лайков")
    void shouldThrowWhenIncrementLikesNoPostTest() {
        when(jdbcTemplate.update(INCREMENT_LIKES, POST_ID)).thenReturn(0);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> postRepository.incrementLikes(POST_ID));
        assertTrue(ex.getMessage().contains("Пост не найден при увеличении лайков"));

        verify(jdbcTemplate).update(INCREMENT_LIKES, POST_ID);
    }
}
