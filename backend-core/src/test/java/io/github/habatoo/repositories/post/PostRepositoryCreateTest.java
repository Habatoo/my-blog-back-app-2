package io.github.habatoo.repositories.post;

import io.github.habatoo.dto.request.PostCreateRequestDto;
import io.github.habatoo.dto.request.PostRequestDto;
import io.github.habatoo.dto.response.PostResponseDto;
import io.github.habatoo.repositories.impl.PostRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.github.habatoo.repositories.sql.PostSqlQueries.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * <h2>Тесты метода createPost для PostRepositoryImpl</h2>
 *
 * <p>
 * Класс покрывает функциональность основных операций над постами:
 * <ul>>
 *     <li>Создание поста с тегами</li>
 * </ul>
 * Для тестирования используются моки JdbcTemplate и вспомогательные RowMapper-ы.
 * Проверяется правильность передаваемых запросов, возвращаемых данных и поведения при ошибках.
 * </p>
 */
@DisplayName("Тесты метода createPost PostRepositoryImpl")
public class PostRepositoryCreateTest extends PostRepositoryTestBase {

    /**
     * Проверяет, что метод createPost добавляет новый пост с тегами, корректно вызывает связанные SQL-запросы,
     * а возвращённый объект содержит все внесённые данные.
     */
    @ParameterizedTest(name = "Создание поста: теги={2}")
    @MethodSource("posts")
    @DisplayName("Должен создать пост с разными вариантами тегов")
    void shouldCreatePostWithAndWithoutTagsTest(
            PostCreateRequestDto createRequest,
            PostResponseDto createdPost,
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
            when(jdbcTemplate.update(eq(DELETE_POST_TAGS), eq(POST_ID))).thenReturn(1);
            when(jdbcTemplate.queryForList(eq(GET_TAGS_FOR_POST), eq(String.class), any())).thenReturn(tags);
        }

        postRepository = Mockito.spy(new PostRepositoryImpl(jdbcTemplate, postListRowMapper));
        PostResponseDto result = postRepository.createPost(createRequest);

        assertEquals(POST_ID, result.id());
        assertEquals(createRequest.title(), result.title());
        assertEquals(createRequest.text(), result.text());
        assertEquals(createRequest.tags(), result.tags());
        verify(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
        verify(jdbcTemplate).queryForObject(eq(SELECT_POST_BY_ID), any(RowMapper.class), eq(POST_ID));

        if (hasTags) {
            verify(jdbcTemplate, times(1)).update(eq(DELETE_POST_TAGS), eq(POST_ID));
            verify(jdbcTemplate, times(2)).update(eq(INSERT_INTO_TAG), any(String.class));
            verify(jdbcTemplate, times(2)).update(eq(INSERT_INTO_POST_TAG), any(Long.class), any(String.class));
        } else {
            verify(jdbcTemplate, never()).update(eq(DELETE_POST_TAGS), anyLong());
            verify(jdbcTemplate, never()).batchUpdate(eq(INSERT_INTO_TAG), anyList(), anyInt(), any());
            verify(jdbcTemplate, never()).batchUpdate(eq(INSERT_INTO_POST_TAG), anyList(), anyInt(), any());
        }
    }

    /**
     * Тест проверяет, что при вызове createPost:
     * - вызывается jdbcTemplate.update с лямбдой PreparedStatementCreator,
     * - параметры PreparedStatement выставляются корректно,
     * - возвращается ожидаемый postId.
     */
    @Test
    void testCreatePostLambdaParametersAndReturnKeyTest() {
        PostRepositoryImpl repository = spy(new PostRepositoryImpl(jdbcTemplate, postListRowMapper));

        doAnswer(invocation -> {
            PreparedStatementCreator psc = invocation.getArgument(0);
            KeyHolder keyHolder = invocation.getArgument(1);
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);

            when(mockConnection.prepareStatement(anyString(), any(String[].class))).thenReturn(mockPs);
            psc.createPreparedStatement(mockConnection);

            verify(mockPs).setString(1, createRequest.title());
            verify(mockPs).setString(2, createRequest.text());
            verify(mockPs).setTimestamp(eq(3), any(Timestamp.class));
            verify(mockPs).setTimestamp(eq(4), any(Timestamp.class));

            keyHolder.getKeyList().add(Map.of("ID", POST_ID));

            return 1;
        }).when(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
        when(jdbcTemplate.queryForObject(eq(SELECT_POST_BY_ID), eq(postListRowMapper), eq(POST_ID)))
                .thenReturn(createdPost);

        PostResponseDto postResponse = repository.createPost(createRequest);

        assertNotNull(postResponse);
    }

    /**
     * Параметризованный тест для проверки ветки updatePostTagsInternal,
     * где tags == null или tags.isEmpty() (ветка не выполняет действия с тегами).
     */
    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("updatePost — не обновляет теги если tags null или пустой")
    void updatePostDoesNotUpdateTagsWhenTagsNullOrEmpty(List<String> tags) {
        PostRepositoryImpl postRepository = spy(new PostRepositoryImpl(jdbcTemplate, postListRowMapper));
        String title = "test";
        String text = "text";

        PostRequestDto createRequest = new PostRequestDto(POST_ID, title, text, tags);

        doReturn(1).when(jdbcTemplate).update(
                eq(UPDATE_POST),
                eq(createRequest.title()),
                eq(createRequest.text()),
                any(LocalDateTime.class),
                eq(POST_ID));
        doReturn(new PostResponseDto(POST_ID, createRequest.title(), createRequest.text(), List.of(), 0, 0))
                .when(jdbcTemplate).queryForObject(
                        eq(SELECT_POST_BY_ID),
                        eq(postListRowMapper),
                        eq(POST_ID));

        PostResponseDto response = postRepository.updatePost(createRequest);

        verify(jdbcTemplate, never()).update(eq(DELETE_POST_TAGS), eq(POST_ID));
        verify(jdbcTemplate, never()).update(eq(INSERT_INTO_TAG), any(String.class));
        verify(jdbcTemplate, never()).update(eq(INSERT_INTO_POST_TAG), eq(POST_ID), any(String.class));
        assertNotNull(response);
        assertEquals(createRequest.id(), response.id());
        assertEquals(createRequest.title(), response.title());
        assertEquals(createRequest.text(), response.text());
    }

    /**
     * Тест для проверки ветки catch (DataIntegrityViolationException ignore) {}.
     */
    @Test
    @DisplayName("updatePost — обрабатывает DataIntegrityViolationException на обновлении тегов/связей")
    void updatePostHandlesDataIntegrityViolationExceptionsOnTagUpdate() {
        PostRepositoryImpl postRepository = spy(new PostRepositoryImpl(jdbcTemplate, postListRowMapper));
        String title = "test";
        String text = "text";

        PostRequestDto createRequest = new PostRequestDto(POST_ID, title, text, tags);

        doReturn(1).when(jdbcTemplate).update(
                eq(UPDATE_POST),
                eq(createRequest.title()),
                eq(createRequest.text()),
                any(LocalDateTime.class),
                eq(POST_ID));

        doReturn(1).when(jdbcTemplate).update(eq(DELETE_POST_TAGS), eq(POST_ID));
        doThrow(new DataIntegrityViolationException("test"))
                .when(jdbcTemplate).update(eq(INSERT_INTO_TAG), any(String.class));
        doThrow(new DataIntegrityViolationException("test"))
                .when(jdbcTemplate).update(eq(INSERT_INTO_POST_TAG), eq(POST_ID), any(String.class));
        doReturn(new PostResponseDto(POST_ID, createRequest.title(), createRequest.text(), tags, 0, 0))
                .when(jdbcTemplate).queryForObject(
                        eq(SELECT_POST_BY_ID),
                        eq(postListRowMapper),
                        eq(POST_ID));

        PostResponseDto response = postRepository.updatePost(createRequest);

        verify(jdbcTemplate, times(1)).update(eq(DELETE_POST_TAGS), eq(POST_ID));
        verify(jdbcTemplate, times(tags.size())).update(eq(INSERT_INTO_TAG), any(String.class));
        verify(jdbcTemplate, times(tags.size())).update(eq(INSERT_INTO_POST_TAG), eq(POST_ID), any(String.class));
        assertNotNull(response);
        assertEquals(createRequest.id(), response.id());
        assertEquals(createRequest.title(), response.title());
        assertEquals(createRequest.text(), response.text());
    }

    /**
     * Проверяет, что при отсутствии ключа в KeyHolder
     * createPost выбрасывает IllegalStateException ("Пост не создан").
     */
    @Test
    void testCreatePostKeyHolderKeyIsNullThrows() {
        PostRepositoryImpl repository = spy(new PostRepositoryImpl(jdbcTemplate, postListRowMapper));
        PostCreateRequestDto createRequest = new PostCreateRequestDto("title", "text", List.of("tag1"));

        doAnswer(invocation -> {
            PreparedStatementCreator psc = invocation.getArgument(0);
            KeyHolder keyHolder = invocation.getArgument(1);
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);

            when(mockConnection.prepareStatement(anyString(), any(String[].class))).thenReturn(mockPs);
            psc.createPreparedStatement(mockConnection);

            verify(mockPs).setString(1, createRequest.title());
            verify(mockPs).setString(2, createRequest.text());
            verify(mockPs).setTimestamp(eq(3), any(Timestamp.class));
            verify(mockPs).setTimestamp(eq(4), any(Timestamp.class));

            return 1;
        }).when(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> repository.createPost(createRequest)
        );
        assertEquals("Пост не создан", ex.getMessage());
    }
}
