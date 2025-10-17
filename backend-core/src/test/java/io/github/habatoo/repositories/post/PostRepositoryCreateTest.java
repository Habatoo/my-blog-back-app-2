package io.github.habatoo.repositories.post;

import io.github.habatoo.dto.request.PostCreateRequestDto;
import io.github.habatoo.dto.request.PostRequestDto;
import io.github.habatoo.dto.response.PostResponseDto;
import io.github.habatoo.repositories.impl.PostRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mockito;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.github.habatoo.repositories.sql.PostSqlQueries.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * <h2>Тесты метода createPost для PostRepositoryImpl</h2>
 *
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
        when(jdbcTemplate.queryForObject(
                eq(SELECT_POST_BY_ID),
                eq(postListRowMapper),
                eq(POST_ID)))
                .thenReturn(createdPost);

        if (hasTags) {
            when(jdbcTemplate.update(eq(DELETE_POST_TAGS), eq(POST_ID))).thenReturn(1);
            when(jdbcTemplate.queryForList(eq(GET_TAGS_FOR_POST), eq(String.class), any())).thenReturn(TAGS);
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
                eq(POST_ID)
        );
        when(jdbcTemplate.queryForObject(
                eq(SELECT_POST_BY_ID),
                eq(postListRowMapper),
                eq(POST_ID)
        )).thenReturn(new PostResponseDto(POST_ID, createRequest.title(), createRequest.text(), List.of(), 0, 0));

        PostResponseDto response = postRepository.updatePost(createRequest);

        verify(jdbcTemplate, never()).update(eq(DELETE_POST_TAGS), eq(POST_ID));
        verify(jdbcTemplate, never()).update(eq(INSERT_INTO_TAG), any(String.class));
        verify(jdbcTemplate, never()).update(eq(INSERT_INTO_POST_TAG), eq(POST_ID), any(String.class));
        assertNotNull(response);
        assertEquals(createRequest.id(), response.id());
        assertEquals(createRequest.title(), response.title());
        assertEquals(createRequest.text(), response.text());
    }
}
