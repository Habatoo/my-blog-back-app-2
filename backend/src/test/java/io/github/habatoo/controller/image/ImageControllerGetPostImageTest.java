package io.github.habatoo.controller.image;

import io.github.habatoo.service.dto.ImageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Тесты для получения изображения поста.
 */
@DisplayName("Тесты метода getPostImage для получения изображения поста.")
class ImageControllerGetPostImageTest extends ImageControllerTestBase {

    @Test
    @DisplayName("Должен вернуть изображение JPEG с правильным Content-Type")
    void shouldReturnJpegImageWithCorrectContentTypeTest() {
        byte[] imageData = createJpegImageData();
        ImageResponse imageResponse = createImageResponse(imageData, MediaType.IMAGE_JPEG);

        when(imageService.getPostImage(VALID_POST_ID)).thenReturn(imageResponse);

        ResponseEntity<byte[]> response = imageController.getPostImage(VALID_POST_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(imageData, response.getBody());
        assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
    }

    @Test
    @DisplayName("Должен вернуть изображение PNG с правильным Content-Type")
    void shouldReturnPngImageWithCorrectContentTypeTest() {
        byte[] imageData = createPngImageData();
        ImageResponse imageResponse = createImageResponse(imageData, MediaType.IMAGE_PNG);

        when(imageService.getPostImage(VALID_POST_ID)).thenReturn(imageResponse);

        ResponseEntity<byte[]> response = imageController.getPostImage(VALID_POST_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(imageData, response.getBody());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
    }

    @Test
    @DisplayName("Должен вернуть пустое изображение с octet-stream Content-Type")
    void shouldReturnEmptyImageWithOctetStreamContentTypeTest() {
        byte[] emptyData = createEmptyImageData();
        ImageResponse imageResponse = createImageResponse(emptyData, MediaType.APPLICATION_OCTET_STREAM);

        when(imageService.getPostImage(VALID_POST_ID)).thenReturn(imageResponse);

        ResponseEntity<byte[]> response = imageController.getPostImage(VALID_POST_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emptyData, response.getBody());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
    }

    @DisplayName("Должен корректно обработать различные идентификаторы постов")
    @ParameterizedTest
    @ValueSource(longs = {1L, 5L, 10L, 50L, 100L})
    void shouldHandleDifferentPostIdsTest(Long postId) {
        byte[] imageData = createJpegImageData();
        ImageResponse imageResponse = createImageResponse(imageData, MediaType.IMAGE_JPEG);

        when(imageService.getPostImage(postId)).thenReturn(imageResponse);

        ResponseEntity<byte[]> response = imageController.getPostImage(postId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(imageData, response.getBody());
    }

    @DisplayName("Должен обработать различные MediaType из сервиса")
    @ParameterizedTest
    @MethodSource("provideMediaTypes")
    void shouldHandleDifferentMediaTypesFromServiceTest(MediaType mediaType) {
        byte[] imageData = createJpegImageData();
        ImageResponse imageResponse = createImageResponse(imageData, mediaType);

        when(imageService.getPostImage(VALID_POST_ID)).thenReturn(imageResponse);

        ResponseEntity<byte[]> response = imageController.getPostImage(VALID_POST_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mediaType, response.getHeaders().getContentType());
    }

    private static Stream<Arguments> provideMediaTypes() {
        return Stream.of(
                Arguments.of(MediaType.IMAGE_JPEG),
                Arguments.of(MediaType.IMAGE_PNG),
                Arguments.of(MediaType.IMAGE_GIF),
                Arguments.of(MediaType.parseMediaType("image/webp")),
                Arguments.of(MediaType.APPLICATION_OCTET_STREAM)
        );
    }
}
