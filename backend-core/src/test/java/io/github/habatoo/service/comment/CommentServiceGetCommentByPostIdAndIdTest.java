package io.github.habatoo.service.comment;

import io.github.habatoo.dto.response.CommentResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты метода getCommentByPostIdAndId класса CommentServiceImpl
 */
@DisplayName("Тесты метода getCommentByPostIdAndId")
class CommentServiceGetCommentByPostIdAndIdTest extends CommentServiceTestBase {

    @Test
    @DisplayName("Должен возвращать комментарий из кеша при наличии")
    void shouldReturnCommentFromCacheIfPresentTest() {
        List<CommentResponseDto> repoComments = List.of(createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, COMMENT_TEXT));
        when(postService.postExists(VALID_POST_ID)).thenReturn(true);
        when(commentRepository.findByPostId(VALID_POST_ID)).thenReturn(repoComments);
        commentService.getCommentsByPostId(VALID_POST_ID);

        Optional<CommentResponseDto> result = commentService.getCommentByPostIdAndId(VALID_POST_ID, VALID_COMMENT_ID);

        assertTrue(result.isPresent());
        assertEquals(VALID_COMMENT_ID, result.get().id());
    }

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
