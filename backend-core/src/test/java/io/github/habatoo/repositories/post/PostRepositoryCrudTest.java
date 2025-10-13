package io.github.habatoo.repositories.post;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.repositories.impl.PostRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.github.habatoo.repositories.sql.PostSqlQueries.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * <h2>Тесты методов findAllPosts, createPost, updatePost, deletePost для PostRepositoryImpl</h2>
 *
 * <p>
 * Класс покрывает функциональность основных операций над постами:
 * <ul>
 *     <li>Поиск всех постов с их тегами</li>
 *     <li>Создание поста с тегами</li>
 *     <li>Обновление данных поста и его тегов</li>
 *     <li>Удаление поста</li>
 *     <li>Обработка ошибки при попытке удалить несуществующий пост</li>
 * </ul>
 * Для тестирования используются моки JdbcTemplate и вспомогательные RowMapper-ы.
 * Проверяется правильность передаваемых запросов, возвращаемых данных и поведения при ошибках.
 * </p>
 */
@DisplayName("Тесты методов findAllPosts, createPost, updatePost, deletePost PostRepositoryImpl.")
class PostRepositoryCrudTest extends PostRepositoryTestBase {

    /**
     * Проверяет, что метод findAllPosts возвращает список всех постов с их тегами.
     * Ожидается вызов двух SQL-запросов: для постов и для тегов.
     */
    @Test
    @DisplayName("Должен вернуть список всех постов с тегами")
    void shouldFindAllPostsWithTagsTest() {
        List<PostResponse> postsWithoutTags = List.of(
                new PostResponse(1L, "Title1", "Text1", List.of(), 0, 0),
                new PostResponse(2L, "Title2", "Text2", List.of(), 0, 0)
        );
        when(jdbcTemplate.query(FIND_ALL_POSTS, postListRowMapper)).thenReturn(postsWithoutTags);
        when(jdbcTemplate.query(eq(GET_TAGS_FOR_POST),
                any(RowMapper.class), anyLong())).thenReturn(List.of("tagA", "tagB"));

        List<PostResponse> result = postRepository.findAllPosts();

        assertEquals(postsWithoutTags.size(), result.size());
        for (PostResponse post : result) {
            assertEquals(List.of("tagA", "tagB"), post.tags());
        }

        verify(jdbcTemplate).query(FIND_ALL_POSTS, postListRowMapper);
    }

    /**
     * Проверяет, что метод createPost добавляет новый пост с тегами, корректно вызывает связанные SQL-запросы,
     * а возвращённый объект содержит все внесённые данные.
     */
    @ParameterizedTest(name = "Создание поста: теги={2}")
    @MethodSource("posts")
    @DisplayName("Должен создать пост с разными вариантами тегов")
    void shouldCreatePostWithAndWithoutTagsTest(
            PostCreateRequest createRequest,
            PostResponse createdPost,
            boolean hasTags) {
        doAnswer(invocation -> {
            KeyHolder kh = invocation.getArgument(1);
            Map<String, Object> keys = Collections.singletonMap("ID", POST_ID);
            kh.getKeyList().add(keys);
            return 1;
        }).when(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
        when(jdbcTemplate.queryForObject(eq(SELECT_POST_BY_ID),
                any(RowMapper.class),
                eq(POST_ID)))
                .thenReturn(createdPost);

        if (hasTags) {
            doReturn(new int[][]{new int[createRequest.tags().size()]})
                    .when(jdbcTemplate).batchUpdate(eq(INSERT_INTO_TAG),
                            eq(createRequest.tags()),
                            eq(createRequest.tags().size()),
                            any(ParameterizedPreparedStatementSetter.class));
            doReturn(new int[][]{new int[createRequest.tags().size()]})
                    .when(jdbcTemplate).batchUpdate(eq(INSERT_INTO_POST_TAG),
                            eq(createRequest.tags()),
                            eq(createRequest.tags().size()),
                            any(ParameterizedPreparedStatementSetter.class));
        }

        when(postRepository.getTagsForPost(POST_ID)).thenReturn(createRequest.tags());

        PostResponse result = postRepository.createPost(createRequest);

        assertEquals(POST_ID, result.id());
        assertEquals(createRequest.title(), result.title());
        assertEquals(createRequest.text(), result.text());
        assertEquals(createRequest.tags(), result.tags());
        verify(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
        verify(jdbcTemplate).queryForObject(eq(SELECT_POST_BY_ID), any(RowMapper.class), eq(POST_ID));

        if (hasTags) {
            verify(jdbcTemplate, times(1)).batchUpdate(eq(INSERT_INTO_TAG), eq(createRequest.tags()), eq(createRequest.tags().size()), any());
            verify(jdbcTemplate, times(1)).batchUpdate(eq(INSERT_INTO_POST_TAG), eq(createRequest.tags()), eq(createRequest.tags().size()), any());
        } else {
            verify(jdbcTemplate, never()).batchUpdate(eq(INSERT_INTO_TAG), anyList(), anyInt(), any());
            verify(jdbcTemplate, never()).batchUpdate(eq(INSERT_INTO_POST_TAG), anyList(), anyInt(), any());
        }
    }

    /**
     * Проверяет, что при создании поста используются корректные лямбды для batchUpdate.
     *
     * <p>
     * Этот тест захватывает переданные в batchUpdate лямбды-обработчики PreparedStatement,
     * и проверяет, что они правильно выставляют параметры в PreparedStatement для каждого тега.
     * Убеждается, что:
     * <ul>
     *   <li>Для вставки тегов в таблицу тегов лямбда вызывает setString с правильным значением.</li>
     *   <li>Для вставки связей пост-тег лямбда вызывается с правильным postId и тегом.</li>
     * </ul>
     * Это обеспечивает правильное формирование параметров batchUpdate на уровне джибкейнов.
     * </p>
     *
     * @throws SQLException в случае ошибок взаимодействия с PreparedStatement при проверке лямбд.
     */
    @Test
    @DisplayName("Должны использоваться корректные лямбды batchUpdate при создании поста")
    void batchUpdateLambdasTest() throws SQLException {
        doAnswer(invocation -> {
            KeyHolder kh = invocation.getArgument(1);
            Map<String, Object> keys = Collections.singletonMap("ID", 1L);
            kh.getKeyList().add(keys);
            return 1;
        }).when(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));

        when(jdbcTemplate.queryForObject(
                eq(SELECT_POST_BY_ID),
                any(RowMapper.class),
                eq(1L)
        )).thenReturn(new PostResponse(1L, createRequest.title(), createRequest.text(), List.of(), 0, 0));

        doReturn(new int[][]{new int[createRequest.tags().size()]}).when(jdbcTemplate).batchUpdate(
                eq(INSERT_INTO_TAG),
                eq(createRequest.tags()),
                eq(createRequest.tags().size()),
                any(ParameterizedPreparedStatementSetter.class)
        );
        doReturn(new int[][]{new int[createRequest.tags().size()]}).when(jdbcTemplate).batchUpdate(
                eq(INSERT_INTO_POST_TAG),
                eq(createRequest.tags()),
                eq(createRequest.tags().size()),
                any(ParameterizedPreparedStatementSetter.class)
        );

        postRepository = Mockito.spy(new PostRepositoryImpl(jdbcTemplate, postListRowMapper));
        doReturn(createRequest.tags()).when(postRepository).getTagsForPost(1L);

        postRepository.createPost(createRequest);
        ArgumentCaptor<ParameterizedPreparedStatementSetter<String>> tagCaptor =
                ArgumentCaptor.forClass(ParameterizedPreparedStatementSetter.class);
        verify(jdbcTemplate).batchUpdate(
                eq(INSERT_INTO_TAG),
                eq(createRequest.tags()),
                eq(createRequest.tags().size()),
                tagCaptor.capture());

        PreparedStatement psTag = mock(PreparedStatement.class);
        tagCaptor.getValue().setValues(psTag, "tag1");
        verify(psTag).setString(1, "tag1");

        ArgumentCaptor<ParameterizedPreparedStatementSetter<String>> postTagCaptor =
                ArgumentCaptor.forClass(ParameterizedPreparedStatementSetter.class);
        verify(jdbcTemplate).batchUpdate(
                eq(INSERT_INTO_POST_TAG),
                eq(createRequest.tags()),
                eq(createRequest.tags().size()),
                postTagCaptor.capture());

        PreparedStatement psPostTag = mock(PreparedStatement.class);
        postTagCaptor.getValue().setValues(psPostTag, "tag1");
        verify(psPostTag).setLong(1, 1L);
        verify(psPostTag).setString(2, "tag1");
    }

    /**
     * Проверяет, что метод updatePost обновляет существующий пост,
     * возвращает обновлённый объект и корректно маппирует теги.
     */
    @Test
    @DisplayName("Должен обновить пост и вернуть обновленный объект с тегами")
    void shouldUpdatePostTest() {
        PostRequest updateRequest = new PostRequest(POST_ID, "Updated Title", "Updated Text", List.of());
        PostResponse updatedPost = new PostResponse(POST_ID, updateRequest.title(), updateRequest.text(), List.of("tag1"), 5, 10);
        when(jdbcTemplate.update(
                eq(UPDATE_POST),
                eq(updateRequest.title()),
                eq(updateRequest.text()),
                any(Timestamp.class),
                eq(updateRequest.id())
        )).thenReturn(1);

        when(jdbcTemplate.queryForObject(
                eq(SELECT_POST_BY_ID),
                any(RowMapper.class),
                eq(updateRequest.id())
        )).thenReturn(updatedPost);

        PostResponse result = postRepository.updatePost(updateRequest);

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

        assertTrue(ex.getMessage().contains("Post to delete not found"));

        verify(jdbcTemplate).update(DELETE_POST, NON_EXISTING_POST_ID);
    }
}
