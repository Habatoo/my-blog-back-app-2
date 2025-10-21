package io.github.habatoo.controllers.image;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doNothing;

/**
 * Тесты для обновления изображения поста.
 */
@DisplayName("Тесты метода updatePostImage для обновления изображения поста.")
class ImageControllerUpdatePostImageTest extends ImageControllerTestBase {

    @Test
    @DisplayName("Должен обновить изображение поста и вернуть 200 статус")
    void shouldUpdatePostImageAndReturnOkStatusTest() {
        doNothing().when(imageService).updatePostImage(VALID_POST_ID, multipartFile);

        ResponseEntity<Void> response = imageController.updatePostImage(VALID_POST_ID, multipartFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @DisplayName("Должен обновить изображение для различных постов")
    @ParameterizedTest
    @ValueSource(longs = {1L, 10L, 100L, 1000L})
    void shouldUpdateImageForDifferentPostsTest(Long postId) {
        doNothing().when(imageService).updatePostImage(postId, multipartFile);

        ResponseEntity<Void> response = imageController.updatePostImage(postId, multipartFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

}
