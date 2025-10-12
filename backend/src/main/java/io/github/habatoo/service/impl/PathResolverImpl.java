package io.github.habatoo.service.impl;

import io.github.habatoo.service.PathResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Сервис для безопасного разрешения и валидации путей файлов.
 * Гарантирует безопасность при работе с файловой системой.
 */
@Component
public class PathResolverImpl implements PathResolver {

    private final Path basePath;

    public PathResolverImpl(@Value("${app.upload.dir:uploads/posts/}") String uploadDir) {
        this.basePath = Paths.get(uploadDir).normalize().toAbsolutePath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolveFilePath(String filename) {
        Path filePath = basePath.resolve(filename).normalize();
        if (!filePath.startsWith(basePath)) {
            throw new SecurityException("Attempt to access file outside upload directory: " + filename);
        }
        return filePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolveFilePath(Path directory, String filename) {
        Path directoryPath = directory.normalize().toAbsolutePath();
        if (!directoryPath.startsWith(basePath)) {
            throw new SecurityException("Attempt to access directory outside upload directory");
        }
        Path filePath = directoryPath.resolve(filename).normalize();
        if (!filePath.startsWith(directoryPath)) {
            throw new SecurityException("Attempt to access file outside post directory: " + filename);
        }
        return filePath;
    }
}
