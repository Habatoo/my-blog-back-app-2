package io.github.habatoo.repositories.post;

import io.github.habatoo.dto.request.PostRequestDto;
import io.github.habatoo.dto.response.PostResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDateTime;
import java.util.List;

import static io.github.habatoo.repositories.sql.PostSqlQueries.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * <h2>Тесты методов findAllPosts, updatePost, deletePost для PostRepositoryImpl</h2>
 *
 * <p>
 * Класс покрывает функциональность основных операций над постами:
 * <ul>
 *     <li>Поиск всех постов с их тегами</li>
 *     <li>Обновление данных поста и его тегов</li>
 *     <li>Удаление поста</li>
 *     <li>Обработка ошибки при попытке удалить несуществующий пост</li>
 * </ul>
 * Для тестирования используются моки JdbcTemplate и вспомогательные RowMapper-ы.
 * Проверяется правильность передаваемых запросов, возвращаемых данных и поведения при ошибках.
 * </p>
 */
@DisplayName("Тесты методов findAllPosts, updatePost, deletePost PostRepositoryImpl.")
class PostRepositoryCrudTest extends PostRepositoryTestBase {

    /**
     * Проверяет, что метод findAllPosts возвращает список всех постов с их тегами.
     * Ожидается вызов двух SQL-запросов: для постов и для тегов.
     */
    @Test
    @DisplayName("Должен вернуть список всех постов с тегами")
    void shouldFindAllPostsWithTagsTest() {
        List<PostResponseDto> postsWithoutTags = List.of(
                new PostResponseDto(1L, "Title1", "Text1", List.of(), 0, 0),
                new PostResponseDto(2L, "Title2", "Text2", List.of(), 0, 0)
        );
        List<String> tags = List.of("tagA", "tagB");
        when(jdbcTemplate.query(FIND_ALL_POSTS, postListRowMapper)).thenReturn(postsWithoutTags);
        when(jdbcTemplate.queryForList(eq(GET_TAGS_FOR_POST), eq(String.class), any())).thenReturn(tags);

        List<PostResponseDto> result = postRepository.findAllPosts();

        assertEquals(postsWithoutTags.size(), result.size());
        for (PostResponseDto post : result) {
            assertEquals(List.of("tagA", "tagB"), post.tags());
        }

        verify(jdbcTemplate).query(FIND_ALL_POSTS, postListRowMapper);
        verify(jdbcTemplate).queryForList(eq(GET_TAGS_FOR_POST), eq(String.class), eq(1L));
        verify(jdbcTemplate).queryForList(eq(GET_TAGS_FOR_POST), eq(String.class), eq(2L));
    }

    /**
     * Проверяет, что метод updatePost обновляет существующий пост,
     * возвращает обновлённый объект и корректно маппирует теги.
     */
    @Test
    @DisplayName("Должен обновить пост и вернуть обновленный объект с тегами")
    void shouldUpdatePostTest() {
        PostRequestDto updateRequest = new PostRequestDto(POST_ID, "Updated Title", "Updated Text", List.of());
        List<String> updatedTags = List.of("tag1");
        PostResponseDto updatedPost = new PostResponseDto(POST_ID, updateRequest.title(), updateRequest.text(), updatedTags, 5, 10);
        when(jdbcTemplate.update(
                eq(UPDATE_POST),
                eq(updateRequest.title()),
                eq(updateRequest.text()),
                any(LocalDateTime.class),
                eq(updateRequest.id())
        )).thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq(SELECT_POST_BY_ID),
                any(RowMapper.class),
                eq(updateRequest.id())
        )).thenReturn(updatedPost);
        when(jdbcTemplate.queryForList(
                eq(GET_TAGS_FOR_POST),
                eq(String.class),
                eq(updateRequest.id())
        )).thenReturn(updatedTags);

        PostResponseDto result = postRepository.updatePost(updateRequest);

        assertEquals(updatedPost, result);
        assertTrue(result.tags().contains("tag1"));
        verify(jdbcTemplate).update(eq(UPDATE_POST), any(), any(), any(), any());
        verify(jdbcTemplate).queryForObject(eq(SELECT_POST_BY_ID), any(RowMapper.class), eq(updateRequest.id()));
    }


    /**
     * Проверяет успешное удаление поста по его идентификатору.
     * Ожидается отсутствие исключений и вызов нужного SQL-запроса.
     */
    @Test
    @DisplayName("Должен удалить пост успешно")
    void shouldDeletePostSuccessfullyTest() {
        when(jdbcTemplate.update(DELETE_POST, POST_ID)).thenReturn(1);

        assertDoesNotThrow(() -> postRepository.deletePost(POST_ID));

        verify(jdbcTemplate).update(DELETE_POST, POST_ID);
    }

    /**
     * Проверяет, что при попытке удалить несуществующий пост будет выброшено IllegalStateException с нужным сообщением.
     */
    @Test
    @DisplayName("Должен выбросить IllegalStateException при удалении несуществующего поста")
    void shouldThrowWhenDeleteNonExistingPostTest() {
        when(jdbcTemplate.update(DELETE_POST, NON_EXISTING_POST_ID))
                .thenReturn(0);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> postRepository.deletePost(NON_EXISTING_POST_ID));

        assertTrue(ex.getMessage().contains("Пост не найден для удаления"));

        verify(jdbcTemplate).update(DELETE_POST, NON_EXISTING_POST_ID);
    }
}
