package io.github.habatoo.controllers.comment;

import io.github.habatoo.dto.request.CommentCreateRequestDto;
import io.github.habatoo.dto.response.CommentResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты обработки создания комментария.
 */
@DisplayName("Тесты метода createComment для обработки создания комментария.")
class CommentControllerCreateCommentTest extends CommentControllerTestBase {

    @DisplayName("Должен создать комментарий и вернуть 201 статус")
    @Test
    void shouldCreateCommentAndReturnCreatedStatusTest() {
        CommentCreateRequestDto createRequest = createCommentCreateRequest(COMMENT_TEXT, VALID_POST_ID);
        CommentResponseDto expectedResponse = createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, COMMENT_TEXT);

        when(commentService.createComment(createRequest)).thenReturn(expectedResponse);

        ResponseEntity<CommentResponseDto> response = commentController.createComment(VALID_POST_ID, createRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(commentService).createComment(createRequest);
    }

    @DisplayName("Должен создать комментарий с различными текстами")
    @ParameterizedTest
    @ValueSource(strings = {
            "Короткий текст",
            "Очень длинный текст комментария с множеством слов и подробным описанием",
            "Текст с спецсимволами !@#$%^&*()",
            "Текст с цифрами 1234567890",
            "   Текст с пробелами   "
    })
    void shouldCreateCommentWithDifferentTextsTest(String text) {
        CommentCreateRequestDto createRequest = createCommentCreateRequest(text, VALID_POST_ID);
        CommentResponseDto expectedResponse = createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, text);

        when(commentService.createComment(createRequest)).thenReturn(expectedResponse);

        ResponseEntity<CommentResponseDto> response = commentController.createComment(VALID_POST_ID, createRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(text, response.getBody().text());
        verify(commentService).createComment(createRequest);
    }

    @DisplayName("Должен создать комментарий для различных постов")
    @ParameterizedTest
    @ValueSource(longs = {1L, 10L, 100L, 1000L})
    void shouldCreateCommentForDifferentPostsTest(Long postId) {
        CommentCreateRequestDto createRequest = createCommentCreateRequest(COMMENT_TEXT, postId);
        CommentResponseDto expectedResponse = createCommentResponse(VALID_COMMENT_ID, postId, COMMENT_TEXT);

        when(commentService.createComment(createRequest)).thenReturn(expectedResponse);

        ResponseEntity<CommentResponseDto> response = commentController.createComment(postId, createRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(postId, response.getBody().postId());
        verify(commentService).createComment(createRequest);
    }
}
