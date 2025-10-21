package io.github.habatoo.repositories.comment;

import io.github.habatoo.dto.response.CommentResponseDto;
import io.github.habatoo.repositories.mapper.CommentRowMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * <h2>Тесты методов findByPostId и findByPostIdAndId репозитория комментариев</h2>
 * Класс проверяет корректность поиска комментариев по postId и по паре postId–commentId.
 *
 */
@DisplayName("Тесты методов поиска комментариев")
class CommentRepositoryFindTest extends CommentRepositoryTestBase {

    /**
     * Тестирует, что findByPostId возвращает корректный список комментариев для заданного postId.
     * Проверяет корректность запроса к JdbcTemplate и совпадение результата с ожиданиями.
     */
    @Test
    @DisplayName("Должен вернуть список комментариев для заданного postId")
    void shouldReturnCommentsByPostIdTest() {
        List<CommentResponseDto> expectedComments = List.of(
                createCommentResponse(COMMENT_ID, POST_ID, COMMENT_TEXT)
        );

        when(jdbcTemplate.query(anyString(), any(CommentRowMapper.class), eq(POST_ID)))
                .thenReturn(expectedComments);

        List<CommentResponseDto> result = commentRepository.findByPostId(POST_ID);

        assertEquals(expectedComments, result);
        verify(jdbcTemplate).query(
                """
                        SELECT id, text, post_id
                        FROM comment
                        WHERE post_id = ?
                        ORDER BY created_at ASC
                        """,
                commentRowMapper,
                POST_ID
        );
    }

    /**
     * Проверяет, что findByPostIdAndId возвращает Optional с комментарием,
     * если комментарий с данным postId и commentId существует.
     * Также убеждается, что JdbcTemplate вызывает запрос с нужными параметрами.
     */
    @Test
    @DisplayName("Должен вернуть Optional с комментарием при существовании по postId и commentId")
    void shouldReturnOptionalCommentByPostIdAndIdFoundTest() {
        List<CommentResponseDto> comments = List.of(createCommentResponse(COMMENT_ID, POST_ID, COMMENT_TEXT));

        when(jdbcTemplate.query(anyString(), any(CommentRowMapper.class), eq(POST_ID), eq(COMMENT_ID)))
                .thenReturn(comments);

        Optional<CommentResponseDto> result = commentRepository.findByPostIdAndId(POST_ID, COMMENT_ID);

        assertTrue(result.isPresent());
        assertEquals(COMMENT_ID, result.get().id());
        verify(jdbcTemplate).query(
                """
                        SELECT id, text, post_id
                        FROM comment
                        WHERE post_id = ? AND id = ?
                        """,
                commentRowMapper,
                POST_ID,
                COMMENT_ID
        );
    }

    /**
     * Гарантирует, что если комментарий по postId и commentId не найден — возвращается пустой Optional.
     */
    @Test
    @DisplayName("Должен вернуть пустой Optional если комментарий не найден")
    void shouldReturnEmptyOptionalIfCommentNotFoundTest() {
        when(jdbcTemplate.query(anyString(), any(CommentRowMapper.class), eq(POST_ID), eq(COMMENT_ID)))
                .thenReturn(Collections.emptyList());

        Optional<CommentResponseDto> result = commentRepository.findByPostIdAndId(POST_ID, COMMENT_ID);

        assertTrue(result.isEmpty());
    }
}
