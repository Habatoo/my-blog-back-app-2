package io.github.habatoo.controller.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты обработки инкремента лайков у постов.
 */
@DisplayName("Тесты метода incrementLikes для обработки инкремента лайков у постов.")
class PostControllerIncrementLikesTest extends PostControllerTestBase {

    @Test
    @DisplayName("Должен увеличить лайки и вернуть обновленное количество")
    void shouldIncrementLikesAndReturnUpdatedCountTest() {
        int expectedLikesCount = 6;

        when(postService.incrementLikes(VALID_POST_ID)).thenReturn(expectedLikesCount);

        ResponseEntity<Integer> response = postController.incrementLikes(VALID_POST_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedLikesCount, response.getBody());
        verify(postService).incrementLikes(VALID_POST_ID);
    }

    @DisplayName("Должен корректно увеличивать лайки для различных постов")
    @ParameterizedTest
    @CsvSource({
            "1, 1",
            "2, 10",
            "3, 100",
            "4, 999"
    })
    void shouldIncrementLikesForDifferentPostsTest(Long postId, int expectedLikes) {
        when(postService.incrementLikes(postId)).thenReturn(expectedLikes);

        ResponseEntity<Integer> response = postController.incrementLikes(postId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedLikes, response.getBody());
        verify(postService).incrementLikes(postId);
    }

    @Test
    @DisplayName("Должен обработать начальное значение лайков")
    void shouldHandleInitialLikesCountTest() {
        when(postService.incrementLikes(VALID_POST_ID)).thenReturn(1);

        ResponseEntity<Integer> response = postController.incrementLikes(VALID_POST_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody());
        verify(postService).incrementLikes(VALID_POST_ID);
    }

}
