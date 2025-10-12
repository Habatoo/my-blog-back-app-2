package io.github.habatoo.controller.comment;

import io.github.habatoo.dto.response.CommentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты обработки получения списка комментариев.
 */
@DisplayName("Тесты метода getCommentsByPostId для получения списка комментариев.")
class CommentControllerGetCommentsTest extends CommentControllerTestBase {

    @Test
    @DisplayName("Должен вернуть список комментариев для существующего поста")
    void shouldReturnCommentsListForExistingPostTest() {
        List<CommentResponse> expectedComments = createCommentList(VALID_POST_ID);

        when(commentService.getCommentsByPostId(VALID_POST_ID))
                .thenReturn(expectedComments);

        List<CommentResponse> actualComments = commentController
                .getCommentsByPostId(VALID_POST_ID);

        assertNotNull(actualComments);
        assertEquals(expectedComments.size(), actualComments.size());
        assertEquals(expectedComments, actualComments);
        verify(commentService).getCommentsByPostId(VALID_POST_ID);
    }

    @Test
    @DisplayName("Должен вернуть пустой список когда у поста нет комментариев")
    void shouldReturnEmptyListWhenPostHasNoCommentsTest() {
        when(commentService.getCommentsByPostId(VALID_POST_ID)).thenReturn(List.of());

        List<CommentResponse> actualComments = commentController
                .getCommentsByPostId(VALID_POST_ID);

        assertNotNull(actualComments);
        assertTrue(actualComments.isEmpty());
        verify(commentService).getCommentsByPostId(VALID_POST_ID);
    }

    @DisplayName("Должен корректно обработать различные идентификаторы постов")
    @ParameterizedTest
    @ValueSource(longs = {1L, 100L, 9999L, Long.MAX_VALUE})
    void shouldHandleDifferentPostIdsTest(Long postId) {
        List<CommentResponse> expectedComments = createCommentList(postId);

        when(commentService.getCommentsByPostId(postId))
                .thenReturn(expectedComments);

        List<CommentResponse> actualComments = commentController.getCommentsByPostId(postId);

        assertEquals(expectedComments, actualComments);
        verify(commentService).getCommentsByPostId(postId);
    }
}
