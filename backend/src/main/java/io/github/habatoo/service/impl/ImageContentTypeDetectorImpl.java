package io.github.habatoo.service.impl;

import io.github.habatoo.service.ImageContentTypeDetector;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * Детектор типа контента изображения на основе анализа содержимого файла.
 * Выполняет  проверку формата изображения.
 */
@Component
public class ImageContentTypeDetectorImpl implements ImageContentTypeDetector {

    /**
     * {@inheritDoc}
     */
    @Override
    public MediaType detect(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            throw new IllegalStateException("Image data cannot be null");
        }

        if (isJpeg(imageData)) return MediaType.IMAGE_JPEG;
        if (isPng(imageData)) return MediaType.IMAGE_PNG;

        return MediaType.APPLICATION_OCTET_STREAM;
    }

    /**
     * Проверяет, является ли содержимое JPEG изображением.
     *
     * @param data массив байт для проверки
     * @return true если данные соответствуют формату JPEG
     */
    private boolean isJpeg(byte[] data) {
        return data.length >= 3 && data[0] == (byte) 0xFF && data[1] == (byte) 0xD8;
    }

    /**
     * Проверяет, является ли содержимое PNG изображением.
     *
     * @param data массив байт для проверки
     * @return true если данные соответствуют формату PNG
     */
    private boolean isPng(byte[] data) {
        return data.length >= 8 && data[0] == (byte) 0x89 && data[1] == 0x50;
    }

}
