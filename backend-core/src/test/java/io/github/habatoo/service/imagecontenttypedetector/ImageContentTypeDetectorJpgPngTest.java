package io.github.habatoo.service.imagecontenttypedetector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тесты для приватного метода isJpeg класса ImageContentTypeDetectorImpl с использованием публичного метода detect.
 *
 * <p>
 * Покрывает все ветки распознавания JPEG и PNG-данных:
 * <ul>
 *   <li>Проверку различных длин массива (меньше и больше требуемого минимального размера)</li>
 *   <li>Корректное определение сигнатуры JPEG и PNG</li>
 *   <li>Обработку неверных/неполных данных (возврат MediaType.APPLICATION_OCTET_STREAM)</li>
 *   <li>Выброс IllegalStateException при пустых данных</li>
 * </ul>
 * Тесты реализованы с помощью параметризованных кейсов и используют только открытый метод detect.
 * </p>
 */
@DisplayName("Тесты методов isJpeg и isPng")
class ImageContentTypeDetectorJpgPngTest extends ImageContentTypeDetectorTestBase {

    /**
     * Проверяет все ветки работы приватного метода isJpeg через публичный API detect:
     * <ul>
     *   <li>Поведение при пустых, недостаточно длинных и некорректных массивах байт</li>
     *   <li>Корректное определение сигнатуры JPEG и возврат подходящего MediaType</li>
     *   <li>Возврат MediaType.APPLICATION_OCTET_STREAM при неверной сигнатуре</li>
     *   <li>Выброс IllegalStateException при пустых данных</li>
     * </ul>
     */
    @ParameterizedTest(name = "JPEG: {2}")
    @MethodSource("validAndInvalidJpeg")
    @DisplayName("Проверка всех веток isJpeg через detect")
    void testIsJpegCases(byte[] imageBytes, MediaType expectedType, String desc) {
        if (imageBytes.length == 0) {
            assertThrows(IllegalStateException.class, () -> contentTypeDetector.detect(imageBytes));
        } else {
            assertEquals(expectedType, contentTypeDetector.detect(imageBytes));
        }
    }

    /**
     * Проверяет все ветки работы приватного метода isPng через публичный API detect:
     * <ul>
     *   <li>Поведение при недостаточной длине данных и некорректной сигнатуре</li>
     *   <li>Корректное определение PNG и возврат MediaType.IMAGE_PNG</li>
     *   <li>Возврат MediaType.APPLICATION_OCTET_STREAM при неверной сигнатуре или недостаточной длине</li>
     * </ul>
     */
    @ParameterizedTest(name = "PNG: {2}")
    @MethodSource("validAndInvalidPng")
    @DisplayName("Проверка всех веток isPng через detect")
    void testIsPngCases(byte[] imageBytes, MediaType expectedType, String desc) {
        if (imageBytes.length == 0) {
            assertThrows(IllegalStateException.class, () -> contentTypeDetector.detect(imageBytes));
        } else {
            assertEquals(expectedType, contentTypeDetector.detect(imageBytes));
        }
    }
}
