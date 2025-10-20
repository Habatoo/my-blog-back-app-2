package io.github.habatoo.service.comment;

import io.github.habatoo.dto.response.CommentResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты для метода обновления комментария в CommentService:
 * проверка сценариев успешного обновления, обработки отсутствия комментария и поста.
 */
@DisplayName("Тесты метода updateComment")
class CommentServiceUpdateCommentTest extends CommentServiceTestBase {

    /**
     * Проверяет, что обновление комментария изменяет текст
     * и комментарий с новым текстом доступен по postId и commentId.
     */
    @Test
    @DisplayName("Должен обновить комментарий")
    void shouldUpdateCommentAndRefreshCacheTest() {
        CommentResponseDto original = createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, COMMENT_TEXT);
        CommentResponseDto updatedComment = createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, UPDATED_COMMENT_TEXT);

        when(commentRepository.update(VALID_POST_ID, VALID_COMMENT_ID, UPDATED_COMMENT_TEXT)).thenReturn(updatedComment);
        when(commentRepository.findByPostId(VALID_POST_ID)).thenReturn(List.of(original));

        commentService.getCommentsByPostId(VALID_POST_ID);

        CommentResponseDto result = commentService.updateComment(createUpdatedCommentRequestDto());

        assertEquals(UPDATED_COMMENT_TEXT, result.text());
        verify(commentRepository).update(VALID_POST_ID, VALID_COMMENT_ID, UPDATED_COMMENT_TEXT);
    }

    /**
     * Проверяет, что попытка обновить несуществующий комментарий вызывает EmptyResultDataAccessException,
     * а репозиторий бросает EmptyResultDataAccessException.
     */
    @Test
    @DisplayName("Должен выбрасывать исключение, если комментарий для обновления не найден")
    void shouldThrowWhenCommentNotFoundForUpdateTest() {
        when(commentRepository.update(VALID_POST_ID, VALID_COMMENT_ID, UPDATED_COMMENT_TEXT))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(EmptyResultDataAccessException.class,
                () -> commentService.updateComment(createUpdatedCommentRequestDto()));
        verify(commentRepository).update(VALID_POST_ID, VALID_COMMENT_ID, UPDATED_COMMENT_TEXT);
    }
}
