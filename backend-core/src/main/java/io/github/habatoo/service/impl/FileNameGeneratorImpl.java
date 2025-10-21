package io.github.habatoo.service.impl;

import io.github.habatoo.service.FileNameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Сервис для генерации уникальных имен файлов.
 * Отвечает за создание безопасных и уникальных имен файлов.
 */
@Slf4j
@Component
public class FileNameGeneratorImpl implements FileNameGenerator {

    private static final int START_NAME_BOUND = 1000;
    private static final int END_NAME_BOUND = 9999;

    private final String defaultExtension;

    public FileNameGeneratorImpl(
            @Value("${app.image.default-extension}") String defaultExtension) {
        this.defaultExtension = defaultExtension;
        log.info("FileNameGenerator инициализирован с расширением по умолчанию: '{}'", defaultExtension);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateFileName(String originalFilename) {
        String fileExtension = getFileExtension(originalFilename);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf(ThreadLocalRandom.current().nextInt(START_NAME_BOUND, END_NAME_BOUND));
        String generated = String.format("%s_%s.%s", timestamp, random, fileExtension);
        log.debug("Сгенерировано имя файла: '{}' для исходного '{}'", generated, originalFilename);

        return generated;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            log.debug("Исходный файл без расширения, используется расширение по умолчанию '{}'", defaultExtension);
            return defaultExtension;
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        if (ext.isEmpty()) {
            log.debug("Исходный файл '{}' без расширения после точки, используется по умолчанию '{}'", filename, defaultExtension);
            return defaultExtension;
        }

        return ext;
    }
}
