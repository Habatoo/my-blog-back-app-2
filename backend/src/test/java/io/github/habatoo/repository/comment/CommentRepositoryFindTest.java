package io.github.habatoo.repository.comment;

import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.repository.mapper.CommentRowMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.github.habatoo.repository.sql.CommentSqlQueries.FIND_BY_POST_ID;
import static io.github.habatoo.repository.sql.CommentSqlQueries.FIND_BY_POST_ID_AND_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * <h2>Тесты методов findByPostId и findByPostIdAndId репозитория комментариев</h2>
 *
 * <p>
 * Класс проверяет корректность поиска комментариев по postId и по паре postId–commentId:
 * <ul>
 *     <li>Если в базе есть комментарии с нужным postId — возвращается корректный список</li>
 *     <li>Если есть комментарий с нужными postId и commentId — возвращается Optional с этим комментарием</li>
 *     <li>Если комментария по обоим id нет — возвращается пустой Optional</li>
 * </ul>
 * Используется мок JdbcTemplate и CommentRowMapper.
 * </p>
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
        List<CommentResponse> expectedComments = List.of(
                createCommentResponse(COMMENT_ID, POST_ID, COMMENT_TEXT)
        );

        when(jdbcTemplate.query(anyString(), any(CommentRowMapper.class), eq(POST_ID)))
                .thenReturn(expectedComments);

        List<CommentResponse> result = commentRepository.findByPostId(POST_ID);

        assertEquals(expectedComments, result);
        verify(jdbcTemplate).query(FIND_BY_POST_ID, commentRowMapper, POST_ID);
    }

    /**
     * Проверяет, что findByPostIdAndId возвращает Optional с комментарием,
     * если комментарий с данным postId и commentId существует.
     * Также убеждается, что JdbcTemplate вызывает запрос с нужными параметрами.
     */
    @Test
    @DisplayName("Должен вернуть Optional с комментарием при существовании по postId и commentId")
    void shouldReturnOptionalCommentByPostIdAndIdFoundTest() {
        List<CommentResponse> comments = List.of(createCommentResponse(COMMENT_ID, POST_ID, COMMENT_TEXT));

        when(jdbcTemplate.query(anyString(), any(CommentRowMapper.class), eq(POST_ID), eq(COMMENT_ID)))
                .thenReturn(comments);

        Optional<CommentResponse> result = commentRepository.findByPostIdAndId(POST_ID, COMMENT_ID);

        assertTrue(result.isPresent());
        assertEquals(COMMENT_ID, result.get().id());
        verify(jdbcTemplate).query(FIND_BY_POST_ID_AND_ID, commentRowMapper, POST_ID, COMMENT_ID);
    }

    /**
     * Гарантирует, что если комментарий по postId и commentId не найден — возвращается пустой Optional.
     */
    @Test
    @DisplayName("Должен вернуть пустой Optional если комментарий не найден")
    void shouldReturnEmptyOptionalIfCommentNotFoundTest() {
        when(jdbcTemplate.query(anyString(), any(CommentRowMapper.class), eq(POST_ID), eq(COMMENT_ID)))
                .thenReturn(Collections.emptyList());

        Optional<CommentResponse> result = commentRepository.findByPostIdAndId(POST_ID, COMMENT_ID);

        assertTrue(result.isEmpty());
    }
}
