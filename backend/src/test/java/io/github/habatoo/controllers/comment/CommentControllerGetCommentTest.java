package io.github.habatoo.controllers.comment;

import io.github.habatoo.dto.response.CommentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты обработки получения комментария по id.
 */
@DisplayName("Тесты метода getCommentByPostIdAndId в CommentController")
class CommentControllerGetCommentTest extends CommentControllerTestBase {

    @Test
    @DisplayName("Должен вернуть комментарий когда он существует")
    void shouldReturnCommentWhenExistsTest() {
        CommentResponse expectedComment = createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, COMMENT_TEXT);

        when(commentService.getCommentByPostIdAndId(VALID_POST_ID, VALID_COMMENT_ID))
                .thenReturn(Optional.of(expectedComment));

        ResponseEntity<CommentResponse> response = commentController
                .getCommentByPostIdAndId(VALID_POST_ID, VALID_COMMENT_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(expectedComment, response.getBody());
        verify(commentService).getCommentByPostIdAndId(VALID_POST_ID, VALID_COMMENT_ID);
    }

    @Test
    @DisplayName("Должен вернуть 404 когда комментарий не найден")
    void shouldReturnNotFoundWhenCommentDoesNotExistTest() {
        when(commentService.getCommentByPostIdAndId(VALID_POST_ID, NON_EXISTENT_COMMENT_ID))
                .thenReturn(Optional.empty());

        ResponseEntity<CommentResponse> response = commentController
                .getCommentByPostIdAndId(VALID_POST_ID, NON_EXISTENT_COMMENT_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.hasBody());
        verify(commentService).getCommentByPostIdAndId(VALID_POST_ID, NON_EXISTENT_COMMENT_ID);
    }

    @Test
    @DisplayName("Должен вернуть 404 когда комментарий не принадлежит посту")
    void shouldReturnNotFoundWhenCommentDoesNotBelongToPostTest() {
        when(commentService.getCommentByPostIdAndId(VALID_POST_ID, VALID_COMMENT_ID))
                .thenReturn(Optional.empty());

        ResponseEntity<CommentResponse> response = commentController
                .getCommentByPostIdAndId(VALID_POST_ID, VALID_COMMENT_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(commentService).getCommentByPostIdAndId(VALID_POST_ID, VALID_COMMENT_ID);
    }

    @DisplayName("Должен корректно обработать различные идентификаторы")
    @ParameterizedTest
    @CsvSource({
            "1, 1",
            "100, 50",
            "9999, 8888",
            "123, 456"
    })
    void shouldHandleDifferentPostAndCommentIdsTest(Long postId, Long commentId) {
        CommentResponse expectedComment = createCommentResponse(commentId, postId, COMMENT_TEXT);

        when(commentService.getCommentByPostIdAndId(postId, commentId))
                .thenReturn(Optional.of(expectedComment));

        ResponseEntity<CommentResponse> response = commentController
                .getCommentByPostIdAndId(postId, commentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedComment, response.getBody());
        verify(commentService).getCommentByPostIdAndId(postId, commentId);
    }
}
