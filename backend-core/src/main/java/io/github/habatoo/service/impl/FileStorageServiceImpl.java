package io.github.habatoo.service.impl;

import io.github.habatoo.service.FileNameGenerator;
import io.github.habatoo.service.FileStorageService;
import io.github.habatoo.service.PathResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Сервис для обработки файлов.
 *
 * @see FileNameGenerator
 * @see PathResolver
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path baseUploadPath;
    private final FileNameGenerator fileNameGenerator;
    private final PathResolver pathResolver;

    public FileStorageServiceImpl(
            @Value("${app.upload.dir:uploads/posts/}") String uploadDir,
            @Value("${app.upload.auto-create-dir:true}") boolean autoCreateDir,
            FileNameGenerator fileNameGenerator,
            PathResolver pathResolver) {

        this.baseUploadPath = Paths.get(uploadDir).normalize().toAbsolutePath();
        this.fileNameGenerator = fileNameGenerator;
        this.pathResolver = pathResolver;

        if (autoCreateDir) {
            createUploadDirectory();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String saveImageFile(Long postId, MultipartFile file) throws IOException {
        log.info("Сохранение файла изображения для поста id={}, оригинальное имя '{}'", postId, file.getOriginalFilename());
        Path postDir = createPostDirectory(postId);
        String fileName = fileNameGenerator.generateFileName(file.getOriginalFilename());
        Path filePath = pathResolver.resolveFilePath(postDir, fileName);
        file.transferTo(filePath);
        log.info("Файл '{}' сохранён по пути '{}'", fileName, filePath);

        return fileName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] loadImageFile(String filename) throws IOException {
        log.debug("Загрузка файла изображения '{}'", filename);
        Path filePath = pathResolver.resolveFilePath(filename);

        return Files.readAllBytes(filePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteImageFile(String filename) throws IOException {
        log.info("Удаление файла изображения '{}'", filename);
        Path filePath = pathResolver.resolveFilePath(filename);

        boolean deleted = Files.deleteIfExists(filePath);
        if (deleted) {
            log.info("Файл '{}' успешно удалён", filename);
        } else {
            log.warn("Файл '{}' не найден для удаления", filename);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePostDirectory(Long postId) {
        Path postDir = baseUploadPath.resolve(postId.toString()).normalize();
        log.info("Удаление директории для поста id={}: '{}'", postId, postDir);

        try {
            if (Files.exists(postDir) && Files.isDirectory(postDir)) {
                try (Stream<Path> paths = Files.walk(postDir)) {
                    paths.sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                    log.debug("Удалён файл/директория '{}'", path);
                                } catch (IOException e) {
                                    log.error("Ошибка при удалении '{}': {}", path, e.getMessage());
                                    throw new RuntimeException("Failed to delete: " + path, e);
                                }
                            });
                }
                log.info("Директория '{}' удалена для поста id={}", postDir, postId);
            }
        } catch (IOException e) {
            log.error("Ошибка при удалении директории '{}' для поста id={}: {}", postDir, postId, e.getMessage());
            throw new RuntimeException("Error deleting post directory: " + e.getMessage(), e);
        }
    }

    private Path createPostDirectory(Long postId) throws IOException {
        Path postDir = baseUploadPath.resolve(postId.toString());
        if (!Files.exists(postDir)) {
            Files.createDirectories(postDir);
            log.debug("Создана директория для поста id={}: '{}'", postId, postDir);
        }

        return postDir;
    }

    private void createUploadDirectory() {
        try {
            if (!Files.exists(baseUploadPath)) {
                Files.createDirectories(baseUploadPath);
                log.info("Создана директория хранения файлов: '{}'", baseUploadPath);
            }
            if (!Files.isDirectory(baseUploadPath)) {
                throw new RuntimeException("Upload directory is not accessible for writing: " + baseUploadPath);
            }
        } catch (IOException e) {
            log.error("Ошибка при создании директории хранения: '{}'", baseUploadPath, e);
            throw new RuntimeException("Failed to create upload directory: " + baseUploadPath, e);
        }
    }
}
