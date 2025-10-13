package io.github.habatoo.controllers.comment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты обработки удаления комментария.
 */
@DisplayName("Тесты метода deleteComment для обработки удаления комментария.")
class CommentControllerDeleteCommentTest extends CommentControllerTestBase {

    @Test
    @DisplayName("Должен удалить комментарий и вернуть 200 статус")
    void shouldDeleteCommentAndReturnOkStatusTest() {
        doNothing().when(commentService).deleteComment(VALID_POST_ID, VALID_COMMENT_ID);

        ResponseEntity<Void> response = commentController.deleteComment(VALID_POST_ID, VALID_COMMENT_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(commentService).deleteComment(VALID_POST_ID, VALID_COMMENT_ID);
    }

    @Test
    @DisplayName("Должен выбросить исключение при удалении несуществующего комментария")
    void shouldThrowExceptionWhenDeletingNonExistentCommentTest() {
        doThrow(new EmptyResultDataAccessException("Comment not found", 1))
                .when(commentService).deleteComment(VALID_POST_ID, NON_EXISTENT_COMMENT_ID);

        assertThrows(EmptyResultDataAccessException.class, () ->
                commentController.deleteComment(VALID_POST_ID, NON_EXISTENT_COMMENT_ID));

        verify(commentService).deleteComment(VALID_POST_ID, NON_EXISTENT_COMMENT_ID);
    }

    @DisplayName("Должен корректно удалять комментарии с различными идентификаторами")
    @ParameterizedTest
    @CsvSource({
            "1, 1",
            "5, 10",
            "100, 50",
            "999, 888"
    })
    void shouldDeleteCommentsWithDifferentIdsTest(Long postId, Long commentId) {
        doNothing().when(commentService).deleteComment(postId, commentId);

        ResponseEntity<Void> response = commentController.deleteComment(postId, commentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(commentService).deleteComment(postId, commentId);
    }

    @DisplayName("Должен обработать граничные значения идентификаторов")
    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, Long.MAX_VALUE - 1, Long.MAX_VALUE})
    void shouldHandleBoundaryIdValuesTest(Long commentId) {
        doNothing().when(commentService).deleteComment(VALID_POST_ID, commentId);

        ResponseEntity<Void> response = commentController.deleteComment(VALID_POST_ID, commentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(commentService).deleteComment(VALID_POST_ID, commentId);
    }
}

