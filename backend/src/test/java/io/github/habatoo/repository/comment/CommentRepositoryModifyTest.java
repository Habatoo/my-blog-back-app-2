package io.github.habatoo.repository.comment;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;

import static io.github.habatoo.repository.sql.CommentSqlQueries.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * <h2>Тесты методов save, updateText и deleteById репозитория комментариев</h2>
 *
 * <p>
 * Класс проверяет корректность работы основных модифицирующих операций репозитория:
 * <ul>
 *     <li>Сохранение нового комментария</li>
 *     <li>Обновление текста комментария</li>
 *     <li>Удаление комментария по идентификатору</li>
 * </ul>
 * Тесты эмулируют работу с базой данных с помощью мока JdbcTemplate
 * и проверяют правильность запросов, а также возвращаемых результатов.
 * </p>
 */
@DisplayName("Тесты методов сохранения, обновления и удаления комментариев")
class CommentRepositoryModifyTest extends CommentRepositoryTestBase {

    /**
     * Проверяет, что метод save правильно сохраняет новый комментарий
     * и возвращает ожидаемый объект CommentResponse.
     * Мокаются параметры запроса и возвращаемое значение из JdbcTemplate.
     */
    @Test
    @DisplayName("Должен сохранить новый комментарий и вернуть созданный объект")
    void shouldSaveNewCommentTest() {
        CommentCreateRequest createRequest = createCommentCreateRequest(COMMENT_TEXT, POST_ID);
        CommentResponse expectedResponse = createCommentResponse(COMMENT_ID, POST_ID, COMMENT_TEXT);

        when(jdbcTemplate.queryForObject(
                eq(INSERT_COMMENT),
                any(RowMapper.class),
                eq(POST_ID),
                eq(COMMENT_TEXT),
                any(Timestamp.class),
                any(Timestamp.class)))
                .thenReturn(expectedResponse);

        CommentResponse result = commentRepository.save(createRequest);

        assertEquals(expectedResponse, result);
        verify(jdbcTemplate).queryForObject(anyString(), any(RowMapper.class),
                eq(POST_ID), eq(COMMENT_TEXT), any(Timestamp.class), any(Timestamp.class));
    }

    /**
     * Проверяет, что метод updateText корректно обновляет текст комментария
     * и возвращает актуализированный объект CommentResponse.
     * Проверяется корректность запроса и возврата результата.
     */
    @Test
    @DisplayName("Должен обновить текст комментария и вернуть обновленный объект")
    void shouldUpdateCommentTextTest() {
        CommentResponse expectedResponse = createCommentResponse(COMMENT_ID, POST_ID, "Updated Text");

        when(jdbcTemplate.queryForObject(
                eq(UPDATE_COMMENT_TEXT),
                any(RowMapper.class),
                eq("Updated Text"),
                any(Timestamp.class),
                eq(COMMENT_ID)))
                .thenReturn(expectedResponse);

        CommentResponse result = commentRepository.updateText(COMMENT_ID, "Updated Text");

        assertEquals(expectedResponse, result);
        verify(jdbcTemplate).queryForObject(anyString(), any(RowMapper.class),
                eq("Updated Text"), any(Timestamp.class), eq(COMMENT_ID));
    }

    /**
     * Проверяет, что метод deleteById вызывает правильный SQL-запрос и возвращает корректное количество удалённых записей.
     */
    @Test
    @DisplayName("Должен удалить комментарий по id и вернуть количество удаленных записей")
    void shouldDeleteCommentByIdTest() {
        when(jdbcTemplate.update(eq(DELETE_COMMENT), eq(COMMENT_ID))).thenReturn(1);

        int deleted = commentRepository.deleteById(COMMENT_ID);

        assertEquals(1, deleted);
        verify(jdbcTemplate).update(DELETE_COMMENT, COMMENT_ID);
    }
}
