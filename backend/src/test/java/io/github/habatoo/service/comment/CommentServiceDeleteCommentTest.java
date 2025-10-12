package io.github.habatoo.service.comment;

import io.github.habatoo.dto.response.CommentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Тесты метода deleteComment класса CommentServiceImpl
 */
@DisplayName("Тесты метода deleteComment")
class CommentServiceDeleteCommentTest extends CommentServiceTestBase {

    @Test
    @DisplayName("Должен удалить комментарий и обновить кеш")
    void shouldDeleteCommentAndUpdateCacheAndPostTest() {
        when(postService.postExists(VALID_POST_ID)).thenReturn(true);
        when(commentRepository.deleteById(VALID_COMMENT_ID)).thenReturn(1);

        CommentResponse comment = createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, COMMENT_TEXT);
        when(commentRepository.findByPostId(VALID_POST_ID)).thenReturn(List.of(comment));

        commentService.getCommentsByPostId(VALID_POST_ID);
        commentService.deleteComment(VALID_POST_ID, VALID_COMMENT_ID);

        verify(postService, times(2)).postExists(VALID_POST_ID); // 2 раза т.к. создавали и удаляли
        verify(commentRepository).deleteById(VALID_COMMENT_ID);
        verify(postService).decrementCommentsCount(VALID_POST_ID);

        Optional<CommentResponse> cachedComment = commentService.getCommentByPostIdAndId(VALID_POST_ID, VALID_COMMENT_ID);
        assertTrue(cachedComment.isEmpty());
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при удалении несуществующего комментария")
    void shouldThrowWhenCommentNotFoundForDeleteTest() {
        when(postService.postExists(VALID_POST_ID)).thenReturn(true);
        when(commentRepository.deleteById(NON_EXISTENT_COMMENT_ID)).thenReturn(0);

        assertThrows(EmptyResultDataAccessException.class,
                () -> commentService.deleteComment(VALID_POST_ID, NON_EXISTENT_COMMENT_ID));

        verify(postService).postExists(VALID_POST_ID);
        verify(commentRepository).deleteById(NON_EXISTENT_COMMENT_ID);
        verify(postService, never()).decrementCommentsCount(anyLong());
    }

    @Test
    @DisplayName("Должен выбрасывать исключение, если пост не существует")
    void shouldThrowIfPostDoesNotExistTest() {
        when(postService.postExists(INVALID_POST_ID)).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> commentService.deleteComment(INVALID_POST_ID, VALID_COMMENT_ID));

        verify(postService).postExists(INVALID_POST_ID);
        verifyNoInteractions(commentRepository);
    }
}
