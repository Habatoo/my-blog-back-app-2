package io.github.habatoo.service.comment;

import io.github.habatoo.dto.response.CommentResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Юнит-тесты для проверки метода удаления комментария в CommentService:
 * успешное удаление, обновление кеша и обработки ошибок.
 */
@DisplayName("Тесты метода deleteComment")
class CommentServiceDeleteCommentTest extends CommentServiceTestBase {

    /**
     * Проверяет, что комментарий успешно удаляется, кеш обновляется, post обновляет счетчик комментариев,
     * и что после удаления комментарий отсутствует в кешированной выдаче.
     */
    @Test
    @DisplayName("Должен удалить комментарий и обновить кеш")
    void shouldDeleteCommentAndUpdateCacheAndPostTest() {
        when(commentRepository.deleteById(VALID_COMMENT_ID)).thenReturn(1);

        CommentResponseDto comment = createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, COMMENT_TEXT);
        when(commentRepository.findByPostId(VALID_POST_ID)).thenReturn(List.of(comment));

        commentService.getCommentsByPostId(VALID_POST_ID);
        commentService.deleteComment(VALID_POST_ID, VALID_COMMENT_ID);

        verify(commentRepository).deleteById(VALID_COMMENT_ID);
        verify(postService).decrementCommentsCount(VALID_POST_ID);

        Optional<CommentResponseDto> cachedComment = commentService.getCommentByPostIdAndId(VALID_POST_ID, VALID_COMMENT_ID);
        assertTrue(cachedComment.isEmpty());
    }

    /**
     * Проверяет, что при попытке удалить несуществующий комментарий выбрасывается EmptyResultDataAccessException
     * и никакие операции над post не выполняются.
     */
    @Test
    @DisplayName("Должен выбрасывать исключение при удалении несуществующего комментария")
    void shouldThrowWhenCommentNotFoundForDeleteTest() {
        when(commentRepository.deleteById(NON_EXISTENT_COMMENT_ID)).thenReturn(0);

        assertThrows(EmptyResultDataAccessException.class,
                () -> commentService.deleteComment(VALID_POST_ID, NON_EXISTENT_COMMENT_ID));

        verify(commentRepository).deleteById(NON_EXISTENT_COMMENT_ID);
        verify(postService, never()).decrementCommentsCount(anyLong());
    }
}
