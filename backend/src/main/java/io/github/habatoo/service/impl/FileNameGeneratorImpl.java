package io.github.habatoo.service.impl;

import io.github.habatoo.service.FileNameGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Сервис для генерации уникальных имен файлов.
 * Отвечает за создание безопасных и уникальных имен файлов.
 */
@Component
public class FileNameGeneratorImpl implements FileNameGenerator {

    private final String defaultExtension;

    public FileNameGeneratorImpl(
            @Value("${app.image.default-extension}") String defaultExtension) {
        this.defaultExtension = defaultExtension;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateFileName(String originalFilename) {
        String fileExtension = getFileExtension(originalFilename);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 9999));
        return String.format("%s_%s.%s", timestamp, random, fileExtension);
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return defaultExtension;
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return ext.isEmpty() ? defaultExtension : ext;
    }
}
