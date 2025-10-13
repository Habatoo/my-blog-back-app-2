package io.github.habatoo.service.comment;

import io.github.habatoo.dto.response.CommentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты метода updateComment класса CommentServiceImpl
 */
@DisplayName("Тесты метода updateComment")
class CommentServiceUpdateCommentTest extends CommentServiceTestBase {

    @Test
    @DisplayName("Должен обновить комментарий и обновить кеш")
    void shouldUpdateCommentAndRefreshCacheTest() {
        when(postService.postExists(VALID_POST_ID)).thenReturn(true);

        CommentResponse original = createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, COMMENT_TEXT);
        CommentResponse updatedComment = createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, UPDATED_COMMENT_TEXT);

        when(commentRepository.updateText(VALID_COMMENT_ID, UPDATED_COMMENT_TEXT)).thenReturn(updatedComment);
        when(commentRepository.findByPostId(VALID_POST_ID)).thenReturn(List.of(original));

        commentService.getCommentsByPostId(VALID_POST_ID);

        CommentResponse result = commentService.updateComment(VALID_POST_ID, VALID_COMMENT_ID, UPDATED_COMMENT_TEXT);

        assertEquals(UPDATED_COMMENT_TEXT, result.text());
        verify(postService, times(2)).postExists(VALID_POST_ID); // 2 раза т.к. создавали и обновляли
        verify(commentRepository).updateText(VALID_COMMENT_ID, UPDATED_COMMENT_TEXT);

        Optional<CommentResponse> cachedUpdated = commentService.getCommentByPostIdAndId(VALID_POST_ID, VALID_COMMENT_ID);
        assertTrue(cachedUpdated.isPresent());
        assertEquals(UPDATED_COMMENT_TEXT, cachedUpdated.get().text());
    }


    @Test
    @DisplayName("Должен выбрасывать исключение, если комментарий для обновления не найден")
    void shouldThrowWhenCommentNotFoundForUpdateTest() {
        when(postService.postExists(VALID_POST_ID)).thenReturn(true);

        when(commentRepository.updateText(VALID_COMMENT_ID, UPDATED_COMMENT_TEXT))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(IllegalStateException.class,
                () -> commentService.updateComment(VALID_POST_ID, VALID_COMMENT_ID, UPDATED_COMMENT_TEXT));
        verify(postService).postExists(VALID_POST_ID);
        verify(commentRepository).updateText(VALID_COMMENT_ID, UPDATED_COMMENT_TEXT);
    }

    @Test
    @DisplayName("Должен выбрасывать исключение, если пост не существует")
    void shouldThrowIfPostDoesNotExistTest() {
        when(postService.postExists(INVALID_POST_ID)).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> commentService.updateComment(INVALID_POST_ID, VALID_COMMENT_ID, UPDATED_COMMENT_TEXT));
        verify(postService).postExists(INVALID_POST_ID);
        verifyNoInteractions(commentRepository);
    }
}
