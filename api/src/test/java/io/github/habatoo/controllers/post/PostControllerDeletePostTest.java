package io.github.habatoo.controllers.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

/**
 * Тесты обработки удаления постов.
 */
@DisplayName("Тесты метода deletePost для обработки удаления постов.")
class PostControllerDeletePostTest extends PostControllerTestBase {

    @Test
    @DisplayName("Должен удалить пост и вернуть 200 статус")
    void shouldDeletePostAndReturnOkStatusTest() {
        doNothing().when(postService).deletePost(VALID_POST_ID);

        ResponseEntity<Void> response = postController.deletePost(VALID_POST_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(postService).deletePost(VALID_POST_ID);
    }

    @DisplayName("Должен корректно удалять посты с различными идентификаторами")
    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 10L, 100L, 1000L})
    void shouldDeletePostsWithDifferentIdsTest(Long postId) {
        doNothing().when(postService).deletePost(postId);

        ResponseEntity<Void> response = postController.deletePost(postId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(postService).deletePost(postId);
    }

    @DisplayName("Должен обработать граничные значения идентификаторов")
    @ParameterizedTest
    @ValueSource(longs = {1L, Long.MAX_VALUE - 1, Long.MAX_VALUE})
    void shouldHandleBoundaryIdValuesTest(Long postId) {
        doNothing().when(postService).deletePost(postId);

        ResponseEntity<Void> response = postController.deletePost(postId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(postService).deletePost(postId);
    }
}
