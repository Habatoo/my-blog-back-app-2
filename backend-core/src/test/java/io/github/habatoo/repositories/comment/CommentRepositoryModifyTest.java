package io.github.habatoo.repositories.comment;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Timestamp;
import java.util.Collections;

import static io.github.habatoo.repositories.sql.CommentSqlQueries.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
     * Мокаются вызовы jdbcTemplate.update() с KeyHolder и последующий вызов queryForObject для получения результата.
     */
    @Test
    @DisplayName("Должен сохранить новый комментарий и вернуть созданный объект")
    void shouldSaveNewCommentTest() {
        CommentCreateRequest createRequest = createCommentCreateRequest(COMMENT_TEXT, POST_ID);
        CommentResponse expectedResponse = createCommentResponse(COMMENT_ID, POST_ID, COMMENT_TEXT);

        when(jdbcTemplate.update(any(PreparedStatementCreator.class), any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder keyHolder = invocation.getArgument(1);
                    (keyHolder).getKeyList().add(Collections.singletonMap("GENERATED_KEY", COMMENT_ID));
                    return 1;
                });
        when(jdbcTemplate.query(
                eq(FIND_BY_ID),
                any(RowMapper.class),
                eq(COMMENT_ID)))
                .thenReturn(Collections.singletonList(expectedResponse));

        CommentResponse result = commentRepository.save(createRequest);

        assertEquals(expectedResponse, result);
        verify(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
        verify(jdbcTemplate).query(eq(FIND_BY_ID), any(RowMapper.class), eq(COMMENT_ID));
    }

    /**
     * Проверяет, что метод updateText корректно обновляет текст комментария
     * и возвращает актуализированный объект CommentResponse.
     * Мокаются вызовы jdbcTemplate.update() и queryForObject().
     */
    @Test
    @DisplayName("Должен обновить текст комментария и вернуть обновленный объект")
    void shouldUpdateCommentTextTest() {
        CommentResponse expectedResponse = createCommentResponse(COMMENT_ID, POST_ID, "Updated Text");

        when(jdbcTemplate.update(
                eq(UPDATE_COMMENT_TEXT),
                eq("Updated Text"),
                any(Timestamp.class),
                eq(COMMENT_ID)))
                .thenReturn(1);
        when(jdbcTemplate.query(
                eq(FIND_BY_ID),
                any(RowMapper.class),
                eq(COMMENT_ID)))
                .thenReturn(Collections.singletonList(expectedResponse));

        CommentResponse result = commentRepository.updateText(COMMENT_ID, "Updated Text");

        assertEquals(expectedResponse, result);
        verify(jdbcTemplate).update(eq(UPDATE_COMMENT_TEXT), eq("Updated Text"), any(Timestamp.class), eq(COMMENT_ID));
        verify(jdbcTemplate).query(eq(FIND_BY_ID), any(RowMapper.class), eq(COMMENT_ID));
    }

    /**
     * Проверяет, что метод save выбрасывает исключение,
     * если после сохранения комментарий не найден в базе.
     * <p>
     * Мокаются вызовы jdbcTemplate.update с KeyHolder и пустой результат поиска.
     */
    @Test
    @DisplayName("Должен бросить исключение, если после сохранения комментарий не найден")
    void shouldThrowWhenSaveFindByIdReturnsEmpty() {
        CommentCreateRequest createRequest = createCommentCreateRequest(COMMENT_TEXT, POST_ID);

        when(jdbcTemplate.update(any(PreparedStatementCreator.class), any(KeyHolder.class))).thenAnswer(invocation -> {
            KeyHolder keyHolder = invocation.getArgument(1);
            (keyHolder).getKeyList().add(Collections.singletonMap("GENERATED_KEY", COMMENT_ID));
            return 1;
        });

        when(jdbcTemplate.query(eq(FIND_BY_ID), any(RowMapper.class), eq(COMMENT_ID)))
                .thenReturn(Collections.emptyList());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> commentRepository.save(createRequest));

        assertEquals("Комментарий не сохранен", exception.getMessage());
        verify(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
        verify(jdbcTemplate).query(eq(FIND_BY_ID), any(RowMapper.class), eq(COMMENT_ID));
    }

    /**
     * Проверяет, что метод updateText выбрасывает исключение,
     * если обновление комментария не затронуло ни одной строки (комментарий не найден для обновления).
     * <p>
     * Мокается вызов jdbcTemplate.update с возвращаемым значением 0.
     */

    @Test
    @DisplayName("Должен бросить исключение, если комментарий для обновления не найден (updatedRows == 0)")
    void shouldThrowExceptionWhenUpdateTextNoRowsUpdated() {
        when(jdbcTemplate.update(
                eq(UPDATE_COMMENT_TEXT),
                eq("Updated Text"),
                any(Timestamp.class),
                eq(COMMENT_ID)))
                .thenReturn(0);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> commentRepository.updateText(COMMENT_ID, "Updated Text"));

        assertEquals("Комментарий с id=" + COMMENT_ID + " не найден для обновления", exception.getMessage());

        verify(jdbcTemplate).update(eq(UPDATE_COMMENT_TEXT), eq("Updated Text"), any(Timestamp.class), eq(COMMENT_ID));
        verify(jdbcTemplate, never()).query(anyString(), any(RowMapper.class), anyLong());
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
