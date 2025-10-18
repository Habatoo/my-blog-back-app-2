package io.github.habatoo.repositories.post;

import io.github.habatoo.dto.request.PostCreateRequestDto;
import io.github.habatoo.dto.response.PostResponseDto;
import io.github.habatoo.repositories.mapper.PostListRowMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.sql.Timestamp;
import java.util.List;

import static io.github.habatoo.repositories.sql.PostSqlQueries.INSERT_INTO_POST_TAG;
import static io.github.habatoo.repositories.sql.PostSqlQueries.INSERT_INTO_TAG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * <h2>Тесты метода createPost для PostRepositoryImpl</h2>
 */
@DisplayName("Тесты метода createPost PostRepositoryImpl")
public class PostRepositoryCreateTest extends PostRepositoryTestBase {

    /**
     * Проверяет, что метод createPost добавляет новый пост с тегами, корректно вызывает связанные SQL-запросы,
     * а возвращённый объект содержит все внесённые данные.
     */
    @ParameterizedTest
    @MethodSource("posts")
    @DisplayName("createPost должен добавлять новый пост с тегами и возвращать корректный PostResponseDto")
    void testCreatePostWithTagsTest(
            PostCreateRequestDto input,
            PostResponseDto expected,
            boolean tagsPresent) {

        when(jdbcTemplate.queryForObject(
                anyString(),
                any(PostListRowMapper.class),
                anyString(),
                anyString(),
                any(Timestamp.class),
                any(Timestamp.class)
        )).thenReturn(new PostResponseDto(POST_ID, input.title(), input.text(), List.of(), 0, 0));
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), eq(POST_ID)))
                .thenReturn(input.tags());
        lenient().when(jdbcTemplate.batchUpdate(anyString(), anyList(), anyInt(), any())).thenReturn(new int[][]{{1}});

        PostResponseDto actual = postRepository.createPost(input);

        assertEquals(expected.id(), actual.id());
        assertEquals(expected.title(), actual.title());
        assertEquals(expected.text(), actual.text());
        assertEquals(expected.tags(), actual.tags());
        assertEquals(expected.likesCount(), actual.likesCount());
        assertEquals(expected.commentsCount(), actual.commentsCount());

        if (tagsPresent) {
            verify(jdbcTemplate, times(1)).batchUpdate(eq(INSERT_INTO_TAG), eq(input.tags()), eq(input.tags().size()), any());
            verify(jdbcTemplate, times(1)).batchUpdate(eq(INSERT_INTO_POST_TAG), eq(input.tags()), eq(input.tags().size()), any());
        } else {
            verify(jdbcTemplate, never()).batchUpdate(eq("INSERT_INTO_TAG"), anyList(), anyInt(), any());
            verify(jdbcTemplate, never()).batchUpdate(eq("INSERT_INTO_POST_TAG"), anyList(), anyInt(), any());
        }

        verify(jdbcTemplate, times(1)).queryForObject(
                anyString(),
                any(PostListRowMapper.class),
                eq(input.title()),
                eq(input.text()),
                any(Timestamp.class),
                any(Timestamp.class)
        );

        verify(jdbcTemplate, times(1))
                .queryForList(anyString(), eq(String.class), eq(POST_ID));
    }

    /**
     * Параметризованный тест для проверки ветки updatePostTagsInternal,
     * где tags == null или tags.isEmpty() (ветка не выполняет действия с тегами).
     */
    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("createPost — ветка без обработки тегов (tags == null или пустой)")
    void testCreatePostWithNoTagsTest(List<String> tags) {

        when(jdbcTemplate.queryForObject(
                anyString(),
                any(PostListRowMapper.class),
                anyString(),
                anyString(),
                any(Timestamp.class),
                any(Timestamp.class)
        )).thenReturn(new PostResponseDto(POST_ID, "title", "text", List.of(), 0, 0));
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), eq(POST_ID))).thenReturn(List.of());

        PostCreateRequestDto input = new PostCreateRequestDto("title", "text", tags);
        PostResponseDto actual = postRepository.createPost(input);

        verify(jdbcTemplate, never()).batchUpdate(eq("INSERT_INTO_TAG"), anyList(), anyInt(), any());
        verify(jdbcTemplate, never()).batchUpdate(eq("INSERT_INTO_POST_TAG"), anyList(), anyInt(), any());

        assertEquals("title", actual.title());
        assertEquals("text", actual.text());
        assertEquals(List.of(), actual.tags());
        assertEquals(0, actual.likesCount());
        assertEquals(0, actual.commentsCount());
    }
}
