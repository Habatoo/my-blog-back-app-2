package io.github.habatoo.controllers.comment;

import io.github.habatoo.dto.request.CommentRequestDto;
import io.github.habatoo.dto.response.CommentResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты обработки редактирования комментария.
 */
@DisplayName("Тесты метода updateComment для обработки редактирования комментария.")
class CommentControllerUpdateCommentTest extends CommentControllerTestBase {

    @Test
    @DisplayName("Должен обновить комментарий и вернуть 200 статус")
    void shouldUpdateCommentAndReturnOkStatusTest() {
        CommentRequestDto updateRequest = createCommentRequest(VALID_COMMENT_ID, UPDATED_COMMENT_TEXT, VALID_POST_ID);
        CommentResponseDto expectedResponse = createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, UPDATED_COMMENT_TEXT);

        when(commentService.updateComment(updateRequest))
                .thenReturn(expectedResponse);

        ResponseEntity<CommentResponseDto> response = commentController.updateComment(
                VALID_POST_ID, VALID_COMMENT_ID, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(commentService).updateComment(updateRequest);
    }

    @Test
    @DisplayName("Должен выбросить исключение при обновлении несуществующего комментария")
    void shouldThrowExceptionWhenUpdatingNonExistentCommentTest() {
        CommentRequestDto updateRequest = createCommentRequest(NON_EXISTENT_COMMENT_ID, UPDATED_COMMENT_TEXT, VALID_POST_ID);

        when(commentService.updateComment(updateRequest))
                .thenThrow(new EmptyResultDataAccessException("Комментарий не найден", 1));

        assertThrows(EmptyResultDataAccessException.class, () ->
                commentController.updateComment(VALID_POST_ID, NON_EXISTENT_COMMENT_ID, updateRequest));

        verify(commentService).updateComment(updateRequest);
    }

    @DisplayName("Должен корректно обновлять комментарии с различными текстами")
    @ParameterizedTest
    @CsvSource({
            "1, 1, Новый текст 1",
            "2, 5, Обновленный комментарий",
            "10, 20, Еще один текст",
            "100, 200, Финальный вариант текста"
    })
    void shouldUpdateCommentsWithDifferentTextsTest(Long postId, Long commentId, String newText) {
        CommentRequestDto updateRequest = createCommentRequest(commentId, newText, postId);
        CommentResponseDto expectedResponse = createCommentResponse(commentId, postId, newText);

        when(commentService.updateComment(updateRequest))
                .thenReturn(expectedResponse);

        ResponseEntity<CommentResponseDto> response = commentController.updateComment(postId, commentId, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(newText, response.getBody().text());
        verify(commentService).updateComment(updateRequest);
    }
}
