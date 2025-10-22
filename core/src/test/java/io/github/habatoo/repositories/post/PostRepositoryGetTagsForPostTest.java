package io.github.habatoo.repositories.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * <h2>Тесты метода getTagsForPost репозитория постов</h2>
 *
 */
@DisplayName("Тесты метода getTagsForPost извлечения тегов по postId.")
public class PostRepositoryGetTagsForPostTest extends PostRepositoryTestBase {

    /**
     * Проверяет, что метод getTagsForPost возвращает ожидаемый список тегов для указанного поста.
     * Используется мок JdbcTemplate, который возвращает предопределённые теги.
     */
    @Test
    @DisplayName("Должен вернуть список тегов для поста")
    void shouldReturnTagsForPostTest() {
        when(jdbcTemplate.queryForList(
                eq("""
                        SELECT t.name FROM tag t
                        JOIN post_tag pt ON t.id = pt.tag_id
                        WHERE pt.post_id = ?
                        """),
                eq(String.class),
                eq(POST_ID))
        ).thenReturn(List.of("tagX", "tagY"));

        List<String> tags = postRepository.getTagsForPost(POST_ID);

        assertEquals(2, tags.size());
        assertTrue(tags.containsAll(List.of("tagX", "tagY")));

        verify(jdbcTemplate).queryForList(
                eq("""
                        SELECT t.name FROM tag t
                        JOIN post_tag pt ON t.id = pt.tag_id
                        WHERE pt.post_id = ?
                        """),
                eq(String.class),
                eq(POST_ID)
        );
    }

    /**
     * Проверяет, что при исключении внутри jdbcTemplate.query метод возвращает пустой список тегов.
     * Это соответствует fail-safe-логике getTagsForPost.
     */
    @Test
    @DisplayName("Должен вернуть пустой список тегов при исключении")
    void shouldReturnEmptyTagsListOnExceptionTest() {
        when(jdbcTemplate.queryForList(
                eq("""
                        SELECT t.name FROM tag t
                        JOIN post_tag pt ON t.id = pt.tag_id
                        WHERE pt.post_id = ?
                        """),
                eq(String.class),
                eq(POST_ID)
        )).thenThrow(RuntimeException.class);

        List<String> tags = postRepository.getTagsForPost(POST_ID);

        assertNotNull(tags);
        assertTrue(tags.isEmpty());

        verify(jdbcTemplate).queryForList(
                eq("""
                        SELECT t.name FROM tag t
                        JOIN post_tag pt ON t.id = pt.tag_id
                        WHERE pt.post_id = ?
                        """),
                eq(String.class),
                eq(POST_ID)
        );
    }

    /**
     * Проверяет, что при выбросе исключения внутри RowMapper-лямбды, метод getTagsForPost возвращает пустой список.
     */
    @Test
    @DisplayName("Тест getTagsForPost — проверка Exception RowMapper для тегов")
    void getTagsForPostExceptionReturnsEmptyListTest() {
        when(jdbcTemplate.queryForList(anyString(), any(String.class), anyLong()))
                .thenThrow(new RuntimeException("DB error"));

        List<String> tags = postRepository.getTagsForPost(123L);
        assertTrue(tags.isEmpty(), "При ошибке должен вернуться пустой список");
    }
}
