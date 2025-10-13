package io.github.habatoo.service.imagevalidator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Тесты метода validateImageUpdate в ImageValidatorImpl")
class ImageValidatorValidateImageUpdateTest extends ImageValidatorTestBase {

    @Test
    @DisplayName("Должен выбросить исключение при null изображении")
    void shouldThrowWhenImageIsNullTest() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> imageValidator.validateImageUpdate(1L, null));
        assertEquals("Image file cannot be null or empty", ex.getMessage());
    }

    @Test
    @DisplayName("Должен выбросить исключение при пустом файле")
    void shouldThrowWhenImageIsEmptyTest() {
        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> imageValidator.validateImageUpdate(1L, emptyFile));
        assertEquals("Image file cannot be null or empty", ex.getMessage());
    }

    @Test
    @DisplayName("Не должен выбрасывать исключение при корректном файле")
    void shouldNotThrowWhenImageIsValidTest() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        assertDoesNotThrow(() -> imageValidator.validateImageUpdate(1L, file));
    }

    @ParameterizedTest
    @DisplayName("Должен выбрасывать исключение при некорректном postId")
    @ValueSource(longs = {0, -1, -100})
    void shouldThrowWhenPostIdInvalidTest(Long postId) {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "filename.jpg",
                "image/jpeg",
                "some image content".getBytes()
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> imageValidator.validateImageUpdate(postId, file));
        assertTrue(ex.getMessage().startsWith("Invalid postId"));
    }

    @Test
    @DisplayName("Не должен выбрасывать исключение для корректного postId")
    void shouldNotThrowWhenPostIdValidTest() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        assertDoesNotThrow(() -> imageValidator.validateImageUpdate(1L, file));
    }
}
