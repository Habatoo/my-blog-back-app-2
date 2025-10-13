package io.github.habatoo.service.comment;

import io.github.habatoo.dto.response.CommentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты метода getCommentsByPostId класса CommentServiceImpl
 */
@DisplayName("Тесты метода getCommentsByPostId")
class CommentServiceGetCommentsByPostIdTest extends CommentServiceTestBase {

    @Test
    @DisplayName("Должен возвращать комментарии из кеша при наличии")
    void shouldReturnCommentsFromCacheIfExistTest() {
        List<CommentResponse> repoComments = List.of(createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, COMMENT_TEXT));
        when(postService.postExists(VALID_POST_ID)).thenReturn(true);
        when(commentRepository.findByPostId(VALID_POST_ID)).thenReturn(repoComments);

        List<CommentResponse> firstCall = commentService.getCommentsByPostId(VALID_POST_ID);
        List<CommentResponse> secondCall = commentService.getCommentsByPostId(VALID_POST_ID);

        assertEquals(repoComments, firstCall);
        assertEquals(repoComments, secondCall);
        verify(postService, times(2)).postExists(VALID_POST_ID);
        verify(commentRepository, times(1)).findByPostId(VALID_POST_ID);
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при несуществующем посте")
    void shouldThrowExceptionIfPostDoesNotExistTest() {
        when(postService.postExists(INVALID_POST_ID)).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> commentService.getCommentsByPostId(INVALID_POST_ID));
        assertTrue(ex.getMessage().contains("Post not found"));
        verify(postService).postExists(INVALID_POST_ID);
        verifyNoInteractions(commentRepository);
    }
}
