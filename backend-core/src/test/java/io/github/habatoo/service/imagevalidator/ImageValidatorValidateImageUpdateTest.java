package io.github.habatoo.service.imagevalidator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты для проверки метода validateImageUpdate(Long, MultipartFile),
 * удостоверяющие корректную обработку ситуаций с некорректным и корректным содержимым изображения.
 */
@DisplayName("Тесты метода validateImageUpdate в ImageValidatorImpl")
class ImageValidatorValidateImageUpdateTest extends ImageValidatorTestBase {

    /**
     * Проверяет, что при передаче null вместо изображения выбрасывается
     * IllegalStateException с ожидаемым сообщением.
     */
    @Test
    @DisplayName("Должен выбросить исключение при null изображении")
    void shouldThrowWhenImageIsNullTest() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> imageValidator.validateImageUpdate(1L, null));
        assertEquals("Image file cannot be null or empty", ex.getMessage());
    }

    /**
     * Проверяет, что при передаче пустого файла выбрасывается IllegalStateException с ожидаемым сообщением.
     */
    @Test
    @DisplayName("Должен выбросить исключение при пустом файле")
    void shouldThrowWhenImageIsEmptyTest() {
        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> imageValidator.validateImageUpdate(1L, emptyFile));
        assertEquals("Image file cannot be null or empty", ex.getMessage());
    }

    /**
     * Проверяет, что для непустого валидного изображения исключение не выбрасывается.
     */
    @Test
    @DisplayName("Не должен выбрасывать исключение при корректном файле")
    void shouldNotThrowWhenImageIsValidTest() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        assertDoesNotThrow(() -> imageValidator.validateImageUpdate(1L, file));
    }
}
