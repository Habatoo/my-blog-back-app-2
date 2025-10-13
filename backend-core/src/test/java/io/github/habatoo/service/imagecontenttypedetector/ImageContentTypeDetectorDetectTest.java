package io.github.habatoo.service.imagecontenttypedetector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для метода detect
 */
@DisplayName("Тесты метода detect")
class ImageContentTypeDetectorDetectTest extends ImageContentTypeDetectorTestBase {

    @Test
    @DisplayName("Должен определить JPEG формат для валидных данных")
    void shouldDetectJpegForValidDataTest() {
        byte[] jpegData = createJpegData();

        MediaType result = contentTypeDetector.detect(jpegData);

        assertEquals(MediaType.IMAGE_JPEG, result);
    }

    @Test
    @DisplayName("Должен определить PNG формат для валидных данных")
    void shouldDetectPngForValidDataTest() {
        byte[] pngData = createPngData();

        MediaType result = contentTypeDetector.detect(pngData);

        assertEquals(MediaType.IMAGE_PNG, result);
    }

    @DisplayName("Должен определить JPEG формат для различных валидных JPEG данных")
    @ParameterizedTest
    @MethodSource("jpegDataProvider")
    void shouldDetectJpegForVariousValidDataTest(byte[] jpegData) {
        MediaType result = contentTypeDetector.detect(jpegData);

        assertEquals(MediaType.IMAGE_JPEG, result);
    }

    @DisplayName("Должен определить PNG формат для различных валидных PNG данных")
    @ParameterizedTest
    @MethodSource("pngDataProvider")
    void shouldDetectPngForVariousValidDataTest(byte[] pngData) {
        MediaType result = contentTypeDetector.detect(pngData);

        assertEquals(MediaType.IMAGE_PNG, result);
    }

    @DisplayName("Должен вернуть OCTET_STREAM для невалидных данных изображения")
    @ParameterizedTest
    @MethodSource("invalidImageDataProvider")
    void shouldReturnOctetStreamForInvalidImageDataTest(byte[] invalidData) {
        MediaType result = contentTypeDetector.detect(invalidData);

        assertEquals(MediaType.APPLICATION_OCTET_STREAM, result);
    }

    @Test
    @DisplayName("Должен выбросить исключение для null данных")
    void shouldThrowExceptionForNullDataTest() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> contentTypeDetector.detect(null));

        assertEquals("Image data cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбросить исключение для пустого массива данных")
    void shouldThrowExceptionForEmptyDataTest() {
        byte[] emptyData = new byte[0];

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> contentTypeDetector.detect(emptyData));

        assertEquals("Image data cannot be null", exception.getMessage());
    }

    @DisplayName("Должен корректно обрабатывать граничные случаи длины данных")
    @ParameterizedTest
    @MethodSource("edgeCasesDataProvider")
    void shouldHandleEdgeCasesDataLengthTest(byte[] data) {
        MediaType result = contentTypeDetector.detect(data);

        assertTrue(result == MediaType.APPLICATION_OCTET_STREAM ||
                result == MediaType.IMAGE_JPEG ||
                result == MediaType.IMAGE_PNG);
    }

    @Test
    @DisplayName("Должен отдавать приоритет JPEG при конфликте сигнатур")
    void shouldPrioritizeJpegOverPngTest() {
        byte[] conflictingData = new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, // JPEG сигнатура
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A // PNG сигнатура
        };

        MediaType result = contentTypeDetector.detect(conflictingData);

        assertEquals(MediaType.IMAGE_JPEG, result);
    }
}
