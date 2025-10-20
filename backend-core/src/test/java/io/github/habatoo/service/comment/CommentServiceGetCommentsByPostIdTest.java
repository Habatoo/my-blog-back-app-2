package io.github.habatoo.service.comment;

import io.github.habatoo.dto.response.CommentResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Юнит-тесты для метода получения списка комментариев по postId в CommentService:
 * проверка загрузки комментария.
 */
@DisplayName("Тесты метода getCommentsByPostId")
class CommentServiceGetCommentsByPostIdTest extends CommentServiceTestBase {

    /**
     * Проверяет, что комментарии для поста возвращаются.
     */
    @Test
    @DisplayName("Должен возвращать комментарии")
    void shouldReturnCommentsFromCacheIfExistTest() {
        List<CommentResponseDto> repoComments = List.of(createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, COMMENT_TEXT));
        when(commentRepository.findByPostId(VALID_POST_ID)).thenReturn(repoComments);

        List<CommentResponseDto> firstCall = commentService.getCommentsByPostId(VALID_POST_ID);

        assertEquals(repoComments, firstCall);
        verify(commentRepository, times(1)).findByPostId(VALID_POST_ID);
    }
}
