package io.github.habatoo.service.comment;

import io.github.habatoo.dto.response.CommentResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Юнит-тесты для метода получения списка комментариев по postId в CommentService:
 * проверка обращения к кешу и корректной обработки несуществующего поста.
 */
@DisplayName("Тесты метода getCommentsByPostId")
class CommentServiceGetCommentsByPostIdTest extends CommentServiceTestBase {

    /**
     * Проверяет, что комментарии для поста возвращаются из кеша, если они уже были загружены,
     * и запрос к репозиторию происходит только один раз.
     */
    @Test
    @DisplayName("Должен возвращать комментарии из кеша при наличии")
    void shouldReturnCommentsFromCacheIfExistTest() {
        List<CommentResponseDto> repoComments = List.of(createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, COMMENT_TEXT));
        when(postService.postExists(VALID_POST_ID)).thenReturn(true);
        when(commentRepository.findByPostId(VALID_POST_ID)).thenReturn(repoComments);

        List<CommentResponseDto> firstCall = commentService.getCommentsByPostId(VALID_POST_ID);
        List<CommentResponseDto> secondCall = commentService.getCommentsByPostId(VALID_POST_ID);

        assertEquals(repoComments, firstCall);
        assertEquals(repoComments, secondCall);
        verify(postService, times(2)).postExists(VALID_POST_ID);
        verify(commentRepository, times(1)).findByPostId(VALID_POST_ID);
    }

    /**
     * Проверяет, что попытка получить комментарии для несуществующего поста вызывает IllegalStateException,
     * и запрос к репозиторию не выполняется.
     */
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
