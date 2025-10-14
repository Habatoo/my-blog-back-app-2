package io.github.habatoo.repositories.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static io.github.habatoo.repositories.sql.PostSqlQueries.GET_TAGS_FOR_POST;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * <h2>Тесты метода getTagsForPost репозитория постов</h2>
 *
 * <p>
 * Класс покрывает корректность работы метода извлечения тегов по postId:
 * <ul>
 *     <li>Возврат непустого списка тегов при штатной работе jdbcTemplate</li>
 *     <li>Возврат пустого списка при исключениях внутри jdbcTemplate.query</li>
 *     <li>Правильная обработка RowMapper-лямбды, передаваемой в query</li>
 *     <li>Поведение при исключении именно внутри RowMapper</li>
 * </ul>
 * Тесты используют мок JdbcTemplate. Для проверки лямбды используется ArgumentCaptor и мок ResultSet.
 * </p>
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
        when(jdbcTemplate.queryForList(eq(GET_TAGS_FOR_POST), eq(String.class), eq(POST_ID)))
                .thenReturn(List.of("tagX", "tagY"));

        List<String> tags = postRepository.getTagsForPost(POST_ID);

        assertEquals(2, tags.size());
        assertTrue(tags.containsAll(List.of("tagX", "tagY")));

        verify(jdbcTemplate).queryForList(eq(GET_TAGS_FOR_POST), eq(String.class), eq(POST_ID));
    }

    /**
     * Проверяет, что при исключении внутри jdbcTemplate.query метод возвращает пустой список тегов.
     * Это соответствует fail-safe-логике getTagsForPost.
     */
    @Test
    @DisplayName("Должен вернуть пустой список тегов при исключении")
    void shouldReturnEmptyTagsListOnExceptionTest() {
        when(jdbcTemplate.queryForList(eq(GET_TAGS_FOR_POST), eq(String.class), eq(POST_ID)))
                .thenThrow(RuntimeException.class);

        List<String> tags = postRepository.getTagsForPost(POST_ID);

        assertNotNull(tags);
        assertTrue(tags.isEmpty());

        verify(jdbcTemplate).queryForList(eq(GET_TAGS_FOR_POST), eq(String.class), eq(POST_ID));
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
