package io.github.habatoo.service.comment;

import io.github.habatoo.dto.response.CommentResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты метода получения комментария по postId и commentId в CommentService:
 * проверка поиска по кешу и по репозиторию, а также сценария отсутствия данных.
 */
@DisplayName("Тесты метода getCommentByPostIdAndId")
class CommentServiceGetCommentByPostIdAndIdTest extends CommentServiceTestBase {

    /**
     * Проверяет, что если в кеше нет комментария, сервис запрашивает его из репозитория.
     */
    @Test
    @DisplayName("Должен возвращать комментарий из репозитория, если кеш пуст")
    void shouldReturnCommentFromRepositoryIfNotCachedTest() {
        when(commentRepository.findByPostIdAndId(VALID_POST_ID, VALID_COMMENT_ID))
                .thenReturn(Optional.of(createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, COMMENT_TEXT)));

        Optional<CommentResponseDto> result = commentService.getCommentByPostIdAndId(VALID_POST_ID, VALID_COMMENT_ID);

        assertTrue(result.isPresent());
        assertEquals(VALID_COMMENT_ID, result.get().id());
        verify(commentRepository).findByPostIdAndId(VALID_POST_ID, VALID_COMMENT_ID);
    }

    /**
     * Проверяет, что если комментарий не найден ни в кеше, ни в репозитории,
     * возвращается пустой Optional.
     */
    @Test
    @DisplayName("Должен возвращать пустой Optional если комментарий не найден")
    void shouldReturnEmptyIfCommentNotFoundTest() {
        when(commentRepository.findByPostIdAndId(VALID_POST_ID, NON_EXISTENT_COMMENT_ID))
                .thenReturn(Optional.empty());

        Optional<CommentResponseDto> result = commentService.getCommentByPostIdAndId(VALID_POST_ID, NON_EXISTENT_COMMENT_ID);

        assertTrue(result.isEmpty());
        verify(commentRepository).findByPostIdAndId(VALID_POST_ID, NON_EXISTENT_COMMENT_ID);
    }
}
