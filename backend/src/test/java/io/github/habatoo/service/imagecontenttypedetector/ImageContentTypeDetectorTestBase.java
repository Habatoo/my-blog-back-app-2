package io.github.habatoo.service.imagecontenttypedetector;

import io.github.habatoo.service.ImageContentTypeDetector;
import io.github.habatoo.service.impl.ImageContentTypeDetectorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.util.stream.Stream;

/**
 * Базовый класс для тестирования ImageContentTypeDetectorImpl
 */
@ExtendWith(MockitoExtension.class)
public abstract class ImageContentTypeDetectorTestBase {

    protected ImageContentTypeDetector contentTypeDetector;

    @BeforeEach
    void setUp() {
        contentTypeDetector = new ImageContentTypeDetectorImpl();
    }

    /**
     * Создает валидные JPEG данные
     */
    protected byte[] createJpegData() {
        return new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x01, 0x02};
    }

    /**
     * Создает валидные PNG данные
     */
    protected byte[] createPngData() {
        return new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x01};
    }

    /**
     * Создает невалидные данные изображения
     */
    protected byte[] createInvalidImageData() {
        return new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
    }

    /**
     * Провайдер данных для тестирования JPEG изображений
     */
    protected static Stream<Arguments> jpegDataProvider() {
        return Stream.of(
                Arguments.of(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}),
                Arguments.of(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0}),
                Arguments.of(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE1}),
                Arguments.of(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xFE})
        );
    }

    /**
     * Провайдер данных для тестирования PNG изображений
     */
    protected static Stream<Arguments> pngDataProvider() {
        return Stream.of(
                Arguments.of(new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}),
                Arguments.of(new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x01}),
                Arguments.of(new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, (byte) 0xFF, (byte) 0xFF})
        );
    }

    /**
     * Провайдер данных для тестирования невалидных изображений
     */
    protected static Stream<Arguments> invalidImageDataProvider() {
        return Stream.of(
                Arguments.of(new byte[]{0x00, 0x01, 0x02}),
                Arguments.of(new byte[]{(byte) 0xFF, (byte) 0xD8}), // неполный JPEG
                Arguments.of(new byte[]{(byte) 0x89, 0x50, 0x4E}), // неполный PNG
                Arguments.of(new byte[]{(byte) 0xFF, (byte) 0xD9, (byte) 0xFF}), // неверная сигнатура JPEG
                Arguments.of(new byte[]{(byte) 0x88, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}) // неверная сигнатура PNG
        );
    }

    /**
     * Провайдер данных для тестирования граничных случаев
     */
    protected static Stream<Arguments> edgeCasesDataProvider() {
        return Stream.of(
                Arguments.of(new byte[2]), // слишком короткий для любого формата
                Arguments.of(new byte[3]), // минимальная длина для JPEG
                Arguments.of(new byte[7]), // почти PNG (8 байт нужно)
                Arguments.of(new byte[8])  // минимальная длина для PNG
        );
    }

    static Stream<Object[]> validAndInvalidJpeg() {
        return Stream.of(
                // data.length < 3
                new Object[]{new byte[]{}, MediaType.APPLICATION_OCTET_STREAM, "empty array"},
                new Object[]{new byte[]{(byte) 0xFF}, MediaType.APPLICATION_OCTET_STREAM, "one byte"},
                new Object[]{new byte[]{(byte) 0xFF, (byte) 0xD8}, MediaType.APPLICATION_OCTET_STREAM, "two bytes"},
                // data.length >= 3, but not JPEG signature
                new Object[]{new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00}, MediaType.APPLICATION_OCTET_STREAM, "wrong signature"},
                // JPEG case
                new Object[]{new byte[]{(byte) 0xFF, (byte) 0xD8, 0x42, 0x01}, MediaType.IMAGE_JPEG, "jpeg proper"}
        );
    }

    static Stream<Object[]> validAndInvalidPng() {
        return Stream.of(
                // data.length == 0
                new Object[]{new byte[]{}, MediaType.APPLICATION_OCTET_STREAM, "empty"},
                // data.length < 8, случайные данные
                new Object[]{new byte[]{1, 2, 3, 4, 5, 6, 7}, MediaType.APPLICATION_OCTET_STREAM, "random 7 bytes"},
                // data.length == 8, но первый байт != 0x89
                new Object[]{new byte[]{0x00, 0x50, 0, 0, 0, 0, 0, 0}, MediaType.APPLICATION_OCTET_STREAM, "first byte wrong"},
                // data.length == 8, первый байт верный, второй байт != 0x50
                new Object[]{new byte[]{(byte) 0x89, 0x00, 0, 0, 0, 0, 0, 0}, MediaType.APPLICATION_OCTET_STREAM, "second byte wrong"},
                // data.length == 8, оба байта верные
                new Object[]{new byte[]{(byte) 0x89, 0x50, 0, 0, 0, 0, 0, 0}, MediaType.IMAGE_PNG, "eight bytes, correct signature"},
                // data.length > 8, оба байта верные
                new Object[]{new byte[]{(byte) 0x89, 0x50, 0, 0, 0, 0, 0, 0, 0x01}, MediaType.IMAGE_PNG, "more than 8 bytes, correct signature"},
                // data.length > 8, первый байт не верный
                new Object[]{new byte[]{0x00, 0x50, 0, 0, 0, 0, 0, 0, 0x01}, MediaType.APPLICATION_OCTET_STREAM, "more than 8, first byte wrong"},
                // data.length > 8, второй байт не верный
                new Object[]{new byte[]{(byte) 0x89, 0x00, 0, 0, 0, 0, 0, 0, 0x01}, MediaType.APPLICATION_OCTET_STREAM, "more than 8, second byte wrong"}
        );
    }
}
