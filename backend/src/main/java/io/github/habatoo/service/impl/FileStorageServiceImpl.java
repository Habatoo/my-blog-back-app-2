package io.github.habatoo.service.impl;

import io.github.habatoo.service.FileNameGenerator;
import io.github.habatoo.service.FileStorageService;
import io.github.habatoo.service.PathResolver;
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
        Path postDir = createPostDirectory(postId);
        String fileName = fileNameGenerator.generateFileName(file.getOriginalFilename());
        Path filePath = pathResolver.resolveFilePath(postDir, fileName);
        file.transferTo(filePath);
        return String.format("%s/%s", postId, fileName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] loadImageFile(String filename) throws IOException {
        Path filePath = pathResolver.resolveFilePath(filename);
        return Files.readAllBytes(filePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteImageFile(String filename) throws IOException {
        Path filePath = pathResolver.resolveFilePath(filename);
        Files.deleteIfExists(filePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePostDirectory(Long postId) {
        Path postDir = baseUploadPath.resolve(postId.toString()).normalize();
        try {
            if (Files.exists(postDir) && Files.isDirectory(postDir)) {
                try (Stream<Path> paths = Files.walk(postDir)) {
                    paths.sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                    throw new RuntimeException("Failed to delete: " + path, e);
                                }
                            });
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error deleting post directory: " + e.getMessage(), e);
        }
    }

    private Path createPostDirectory(Long postId) throws IOException {
        Path postDir = baseUploadPath.resolve(postId.toString());
        if (!Files.exists(postDir)) {
            Files.createDirectories(postDir);
        }
        return postDir;
    }

    private void createUploadDirectory() {
        try {
            if (!Files.exists(baseUploadPath)) {
                Files.createDirectories(baseUploadPath);
            }
            if (!Files.isDirectory(baseUploadPath)) {
                throw new RuntimeException("Upload directory is not accessible for writing: " + baseUploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory: " + baseUploadPath, e);
        }
    }
}
