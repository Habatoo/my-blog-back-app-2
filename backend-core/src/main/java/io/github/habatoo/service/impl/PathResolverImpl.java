package io.github.habatoo.service.impl;

import io.github.habatoo.service.PathResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Сервис для безопасного разрешения и валидации путей файлов.
 * Гарантирует безопасность при работе с файловой системой.
 */
@Slf4j
@Component
public class PathResolverImpl implements PathResolver {

    private final Path basePath;

    public PathResolverImpl(@Value("${app.upload.dir:uploads/posts/}") String uploadDir) {
        this.basePath = Paths.get(uploadDir).normalize().toAbsolutePath();
        log.info("PathResolver инициализирован с корневым путём: '{}'", basePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolveFilePath(String filename) {
        Path filePath = basePath.resolve(filename).normalize();
        if (!filePath.startsWith(basePath)) {
            log.error("Попытка доступа к файлу вне директории: '{}'", filename);
            throw new SecurityException("Attempt to access file outside upload directory: " + filename);
        }
        log.debug("Резолв файла: '{}' -> '{}'", filename, filePath);

        return filePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolveFilePath(Path directory, String filename) {
        Path directoryPath = directory.normalize().toAbsolutePath();
        if (!directoryPath.startsWith(basePath)) {
            log.error("Попытка доступа к директории вне upload: '{}'", directoryPath);
            throw new SecurityException("Attempt to access directory outside upload directory");
        }
        Path filePath = directoryPath.resolve(filename).normalize();
        if (!filePath.startsWith(directoryPath)) {
            log.error("Попытка доступа к файлу вне директории поста: '{}' -> '{}'", filename, filePath);
            throw new SecurityException("Attempt to access file outside post directory: " + filename);
        }
        log.debug("Резолв файла из директории: '{}' + '{}' -> '{}'", directoryPath, filename, filePath);

        return filePath;
    }
}
