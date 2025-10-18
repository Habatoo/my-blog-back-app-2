package io.github.habatoo.service.comment;

import io.github.habatoo.dto.response.CommentResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Юнит-тесты для метода обновления комментария в CommentService:
 * проверка сценариев успешного обновления, обработки отсутствия комментария и поста.
 */
@DisplayName("Тесты метода updateComment")
class CommentServiceUpdateCommentTest extends CommentServiceTestBase {

    /**
     * Проверяет, что обновление комментария изменяет текст, обновляет кеш,
     * и комментарий с новым текстом доступен по postId и commentId.
     */
    @Test
    @DisplayName("Должен обновить комментарий и обновить кеш")
    void shouldUpdateCommentAndRefreshCacheTest() {
        when(postService.postExists(VALID_POST_ID)).thenReturn(true);

        CommentResponseDto original = createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, COMMENT_TEXT);
        CommentResponseDto updatedComment = createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, UPDATED_COMMENT_TEXT);

        when(commentRepository.update(VALID_POST_ID, VALID_COMMENT_ID, UPDATED_COMMENT_TEXT)).thenReturn(updatedComment);
        when(commentRepository.findByPostId(VALID_POST_ID)).thenReturn(List.of(original));

        commentService.getCommentsByPostId(VALID_POST_ID);

        CommentResponseDto result = commentService.updateComment(createUpdatedCommentRequestDto());

        assertEquals(UPDATED_COMMENT_TEXT, result.text());
        verify(postService, times(2)).postExists(VALID_POST_ID); // 2 раза т.к. создавали и обновляли
        verify(commentRepository).update(VALID_POST_ID, VALID_COMMENT_ID, UPDATED_COMMENT_TEXT);

        Optional<CommentResponseDto> cachedUpdated = commentService.getCommentByPostIdAndId(VALID_POST_ID, VALID_COMMENT_ID);
        assertTrue(cachedUpdated.isPresent());
        assertEquals(UPDATED_COMMENT_TEXT, cachedUpdated.get().text());
    }

    /**
     * Проверяет, что попытка обновить несуществующий комментарий вызывает EmptyResultDataAccessException,
     * а репозиторий бросает EmptyResultDataAccessException.
     */
    @Test
    @DisplayName("Должен выбрасывать исключение, если комментарий для обновления не найден")
    void shouldThrowWhenCommentNotFoundForUpdateTest() {
        when(postService.postExists(VALID_POST_ID)).thenReturn(true);

        when(commentRepository.update(VALID_POST_ID, VALID_COMMENT_ID, UPDATED_COMMENT_TEXT))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(EmptyResultDataAccessException.class,
                () -> commentService.updateComment(createUpdatedCommentRequestDto()));
        verify(postService).postExists(VALID_POST_ID);
        verify(commentRepository).update(VALID_POST_ID, VALID_COMMENT_ID, UPDATED_COMMENT_TEXT);
    }

    /**
     * Проверяет, что при обновлении комментария у несуществующего поста возникает IllegalStateException
     * и доступ к репозиторию не происходит.
     */
    @Test
    @DisplayName("Должен выбрасывать исключение, если пост не существует")
    void shouldThrowIfPostDoesNotExistTest() {
        when(postService.postExists(VALID_POST_ID)).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> commentService.updateComment(createUpdatedCommentRequestDto()));
        verify(postService).postExists(VALID_POST_ID);
        verifyNoInteractions(commentRepository);
    }
}
