package io.github.habatoo.repositories.post;

import io.github.habatoo.dto.request.PostCreateRequestDto;
import io.github.habatoo.dto.response.PostResponseDto;
import io.github.habatoo.repositories.impl.PostRepositoryImpl;
import io.github.habatoo.repositories.mapper.PostListRowMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

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
            verify(jdbcTemplate, times(1)).batchUpdate(
                    eq("""
                            INSERT INTO tag (name)
                            VALUES (?)
                            ON CONFLICT (name) DO NOTHING;
                            """),
                    eq(input.tags()),
                    eq(input.tags().size()),
                    any()
            );
            verify(jdbcTemplate, times(1)).batchUpdate(
                    eq("""
                            INSERT INTO post_tag (post_id, tag_id)
                            VALUES (?, (SELECT id FROM tag WHERE name = ?))
                            ON CONFLICT (post_id, tag_id) DO NOTHING;
                            """),
                    eq(input.tags()),
                    eq(input.tags().size()),
                    any());
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

    /**
     * Проверяет, что при создании поста используются корректные лямбды для batchUpdate.
     *
     * <p>
     * Тест захватывает переданные в batchUpdate лямбды-обработчики PreparedStatement,
     * и проверяет, что они правильно выставляют параметры в PreparedStatement для каждого тега.
     * Это обеспечивает правильное формирование параметров batchUpdate на уровне джибкейнов.
     * </p>
     *
     * @throws SQLException в случае ошибок взаимодействия с PreparedStatement при проверке лямбд.
     */
    @Test
    @DisplayName("Должны использоваться корректные лямбды batchUpdate при создании поста")
    void batchUpdateLambdasTest() throws SQLException {
        PostCreateRequestDto createRequest = new PostCreateRequestDto(TITLE, TEXT, TAGS);
        PostResponseDto response = createPostDto(POST_ID, List.of());

        when(jdbcTemplate.queryForObject(
                eq("""
                        INSERT INTO post (title, text, likes_count, comments_count, created_at, updated_at)
                        VALUES (?, ?, 0, 0, ?, ?)
                        RETURNING id, title, text, likes_count, comments_count
                        """),
                eq(postListRowMapper),
                eq(TITLE),
                eq(TEXT),
                any(Timestamp.class),
                any(Timestamp.class)
        )).thenReturn(response);

        when(jdbcTemplate.update(eq(
                        """
                                DELETE FROM post_tag WHERE post_id = ?
                                """),
                eq(POST_ID)
        )).thenReturn(1);

        doReturn(new int[][]{new int[createRequest.tags().size()]})
                .when(jdbcTemplate).batchUpdate(
                        eq("""
                                INSERT INTO tag (name)
                                VALUES (?)
                                ON CONFLICT (name) DO NOTHING;
                                """),
                        eq(createRequest.tags()),
                        eq(createRequest.tags().size()),
                        any(ParameterizedPreparedStatementSetter.class)
                );

        doReturn(new int[][]{new int[createRequest.tags().size()]})
                .when(jdbcTemplate).batchUpdate(
                        eq("""
                                INSERT INTO post_tag (post_id, tag_id)
                                VALUES (?, (SELECT id FROM tag WHERE name = ?))
                                ON CONFLICT (post_id, tag_id) DO NOTHING;
                                """),
                        eq(createRequest.tags()),
                        eq(createRequest.tags().size()),
                        any(ParameterizedPreparedStatementSetter.class)
                );

        postRepository = Mockito.spy(new PostRepositoryImpl(jdbcTemplate, postListRowMapper));
        postRepository.createPost(createRequest);

        ArgumentCaptor<ParameterizedPreparedStatementSetter<String>> tagSetterCaptor =
                ArgumentCaptor.forClass(ParameterizedPreparedStatementSetter.class);
        verify(jdbcTemplate).batchUpdate(
                eq("""
                        INSERT INTO tag (name)
                        VALUES (?)
                        ON CONFLICT (name) DO NOTHING;
                        """),
                eq(createRequest.tags()),
                eq(createRequest.tags().size()),
                tagSetterCaptor.capture());

        PreparedStatement psTag = mock(PreparedStatement.class);
        tagSetterCaptor.getValue().setValues(psTag, "tag1");
        verify(psTag).setString(1, "tag1");

        ArgumentCaptor<ParameterizedPreparedStatementSetter<String>> postTagSetterCaptor =
                ArgumentCaptor.forClass(ParameterizedPreparedStatementSetter.class);
        verify(jdbcTemplate).batchUpdate(
                eq("""
                        INSERT INTO post_tag (post_id, tag_id)
                        VALUES (?, (SELECT id FROM tag WHERE name = ?))
                        ON CONFLICT (post_id, tag_id) DO NOTHING;
                        """),
                eq(createRequest.tags()),
                eq(createRequest.tags().size()),
                postTagSetterCaptor.capture()
        );

        PreparedStatement psPostTag = mock(PreparedStatement.class);
        postTagSetterCaptor.getValue().setValues(psPostTag, "tag1");
        verify(psPostTag).setLong(1, 1L);
        verify(psPostTag).setString(2, "tag1");
    }
}
