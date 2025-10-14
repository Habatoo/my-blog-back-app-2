package io.github.habatoo.repositories.comment;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.github.habatoo.repositories.sql.CommentSqlQueries.*;
import static org.junit.jupiter.api.Assertions.*;
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

        CommentResponse result = commentRepository.updateText(POST_ID, COMMENT_ID, "Updated Text");

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
                () -> commentRepository.updateText(POST_ID, COMMENT_ID, "Updated Text"));

        assertEquals("Комментарий с id=" + COMMENT_ID + " не найден для обновления", exception.getMessage());

        verify(jdbcTemplate).update(eq(UPDATE_COMMENT_TEXT), eq("Updated Text"), any(Timestamp.class), eq(COMMENT_ID));
        verify(jdbcTemplate, never()).query(anyString(), any(RowMapper.class), anyLong());
    }

    /**
     * Тестирует ветку метода updateText, когда обновление прошло (updatedRows != 0),
     * но после обновления комментарий с заданным id не найден в базе.
     * <p>
     * Метод должен выбросить IllegalStateException с сообщением "Комментарий не обновлен",
     * если результат запроса пустой.
     */
    @Test
    @DisplayName("updateText — бросает IllegalStateException если комментария нет после обновления")
    void testUpdateTextThrowsIfCommentNotFoundAfterUpdateTest() {
        when(jdbcTemplate.update(anyString(), any(), any(), eq(COMMENT_ID))).thenReturn(1);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(COMMENT_ID)))
                .thenReturn(Collections.emptyList());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> commentRepository.updateText(POST_ID, COMMENT_ID, COMMENT_NEW_TEXT)
        );
        assertEquals("Комментарий не обновлен", ex.getMessage());
    }

    /**
     * Тестирует, что при сохранении нового комментария
     * параметры postId, text, created_at, updated_at корректно пробрасываются в лямбду
     * connection -> PreparedStatement.
     * <p>
     * Проверка реализована с помощью аргумент-каптора для лямбды и моков.
     */
    @Test
    @DisplayName("save — провалидация параметров лямбды через ArgumentCaptor")
    void testSaveCommentLambdaParametersWithCaptor() {
        CommentCreateRequest createRequest = createCommentCreateRequest(COMMENT_TEXT, POST_ID);
        CommentResponse expectedResponse = createCommentResponse(COMMENT_ID, POST_ID, COMMENT_TEXT);

        doAnswer(invocation -> {
            PreparedStatementCreator creator = invocation.getArgument(0);
            Connection con = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            KeyHolder kh = invocation.getArgument(1);
            Map<String, Object> keys = Collections.singletonMap("ID", 2L);
            kh.getKeyList().add(keys);
            when(con.prepareStatement(anyString(), any(String[].class))).thenReturn(ps);
            creator.createPreparedStatement(con);

            verify(ps).setLong(eq(1), eq(POST_ID));
            verify(ps).setString(eq(2), eq(COMMENT_TEXT));
            verify(ps).setTimestamp(eq(3), any(Timestamp.class));
            verify(ps).setTimestamp(eq(4), any(Timestamp.class));
            return null;
        }).when(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));

        when(jdbcTemplate.query(eq(FIND_BY_ID), any(RowMapper.class), eq(COMMENT_ID)))
                .thenReturn(List.of(expectedResponse));

        CommentResponse response = commentRepository.save(createRequest);

        assertNotNull(response);
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
