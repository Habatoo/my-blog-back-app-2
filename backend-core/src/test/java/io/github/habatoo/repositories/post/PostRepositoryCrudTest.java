package io.github.habatoo.repositories.post;

import io.github.habatoo.dto.request.PostRequestDto;
import io.github.habatoo.dto.response.PostResponseDto;
import io.github.habatoo.repositories.impl.PostRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
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
    void updatePostShouldReturnUpdatedPostWithTags() {
        PostRequestDto requestDto = new PostRequestDto(POST_ID, TITLE, TEXT, TAGS);
        PostResponseDto postReturned = createPostDto(POST_ID, List.of());

        when(jdbcTemplate.queryForObject(anyString(), eq(postListRowMapper), any(), any(), any(), any())).thenReturn(postReturned);
        when(jdbcTemplate.update(eq(DELETE_POST_TAGS), eq(POST_ID))).thenReturn(1);
        when(jdbcTemplate.batchUpdate(eq(INSERT_INTO_TAG), anyList(), anyInt(), any())).thenReturn(new int[][]{});
        when(jdbcTemplate.batchUpdate(eq(INSERT_INTO_POST_TAG), anyList(), anyInt(), any())).thenReturn(new int[][]{});
        when(jdbcTemplate.queryForList(eq(GET_TAGS_FOR_POST), eq(String.class), eq(POST_ID))).thenReturn(TAGS);

        PostResponseDto result = postRepository.updatePost(requestDto);

        assertNotNull(result);
        assertEquals(POST_ID, result.id());
        assertEquals(TAGS, result.tags());
        verify(jdbcTemplate).queryForObject(anyString(), eq(postListRowMapper), any(), any(), any(), any());
        verify(jdbcTemplate).batchUpdate(eq(INSERT_INTO_TAG), anyList(), anyInt(), any());
        verify(jdbcTemplate).batchUpdate(eq(INSERT_INTO_POST_TAG), anyList(), anyInt(), any());
        verify(jdbcTemplate).queryForList(anyString(), eq(String.class), eq(POST_ID));
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
