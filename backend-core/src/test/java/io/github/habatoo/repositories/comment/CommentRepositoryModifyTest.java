package io.github.habatoo.repositories.comment;

import io.github.habatoo.dto.request.CommentCreateRequestDto;
import io.github.habatoo.dto.response.CommentResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;

import static io.github.habatoo.repositories.sql.CommentSqlQueries.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * <h2>Тесты методов save, updateText и deleteById репозитория комментариев</h2>
 * Класс проверяет корректность работы основных модифицирующих операций репозитория.
 */
@DisplayName("Тесты методов сохранения, обновления и удаления комментариев")
class CommentRepositoryModifyTest extends CommentRepositoryTestBase {

    /**
     * Проверяет, что метод save правильно сохраняет новый комментарий
     * и возвращает ожидаемый объект CommentResponseDto.
     * Мокаются вызовы jdbcTemplate.update() с KeyHolder и последующий вызов queryForObject для получения результата.
     */
    @Test
    @DisplayName("Должен сохранить новый комментарий и вернуть созданный объект")
    void shouldSaveNewCommentTest() {
        CommentCreateRequestDto createRequest = createCommentCreateRequest(COMMENT_TEXT, POST_ID);
        CommentResponseDto expectedResponse = createCommentResponse(COMMENT_ID, POST_ID, COMMENT_TEXT);

        when(jdbcTemplate.queryForObject(
                eq(INSERT_COMMENT),
                any(RowMapper.class),
                eq(createRequest.postId()),
                eq(createRequest.text()),
                any(Timestamp.class),
                any(Timestamp.class)
        )).thenReturn(expectedResponse);

        CommentResponseDto result = commentRepository.save(createRequest);

        assertEquals(expectedResponse, result);
        verify(jdbcTemplate).queryForObject(
                eq(INSERT_COMMENT),
                any(RowMapper.class),
                eq(createRequest.postId()),
                eq(createRequest.text()),
                any(Timestamp.class),
                any(Timestamp.class));
    }

    /**
     * Проверяет, что метод update корректно обновляет текст комментария
     * и возвращает актуализированный объект CommentResponseDto.
     * Мокаются вызовы jdbcTemplate.update() и queryForObject().
     */
    @Test
    @DisplayName("Должен обновить текст комментария и вернуть обновленный объект")
    void shouldUpdateCommentTextTest() {
        CommentResponseDto expectedResponse = createCommentResponse(COMMENT_ID, POST_ID, UPDATED_TEXT);

        when(jdbcTemplate.queryForObject(
                eq(UPDATE_COMMENT_TEXT),
                any(RowMapper.class),
                eq(UPDATED_TEXT),
                any(Timestamp.class),
                eq(COMMENT_ID))
        ).thenReturn(expectedResponse);

        CommentResponseDto result = commentRepository.update(POST_ID, COMMENT_ID, UPDATED_TEXT);

        assertEquals(expectedResponse, result);
        verify(jdbcTemplate).queryForObject(
                eq(UPDATE_COMMENT_TEXT),
                any(RowMapper.class),
                eq(UPDATED_TEXT),
                any(Timestamp.class),
                eq(COMMENT_ID)
        );
    }

    @Test
    @DisplayName("Должен удалить комментарий по id и вернуть количество удалённых строк")
    void shouldDeleteCommentByIdTest() {
        when(jdbcTemplate.update(eq(DELETE_COMMENT), eq(COMMENT_ID))).thenReturn(1);

        int deletedRows = commentRepository.deleteById(COMMENT_ID);

        assertEquals(1, deletedRows);
        verify(jdbcTemplate).update(DELETE_COMMENT, COMMENT_ID);
    }
}
