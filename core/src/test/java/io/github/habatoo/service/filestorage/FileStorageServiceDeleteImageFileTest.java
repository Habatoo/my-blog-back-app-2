package io.github.habatoo.service.filestorage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

/**
 * Тесты для метода resolveFilePath.
 */
@DisplayName("Тесты метода resolveFilePath")
class FileStorageServiceDeleteImageFileTest extends FileStorageServiceTestBase {

    @Test
    @DisplayName("Должен удалить файл изображения")
    void shouldDeleteImageFileTest() throws IOException {
        String filename = "123/image.jpg";
        byte[] content = "content".getBytes();
        Path filePath = baseUploadPath.resolve(filename);

        createTestFile(filePath, content);
        when(pathResolver.resolveFilePath(filename)).thenReturn(filePath);

        fileStorageService.deleteImageFile(filename);

        assertFalse(fileExists(filePath));
    }

    @Test
    @DisplayName("Должен игнорировать удаление несуществующего файла")
    void shouldIgnoreDeletingNonExistentFileTest() {
        String filename = "nonexistent/file.jpg";
        Path filePath = baseUploadPath.resolve(filename);

        when(pathResolver.resolveFilePath(filename)).thenReturn(filePath);

        assertDoesNotThrow(() -> fileStorageService.deleteImageFile(filename));
    }

    @DisplayName("Должен удалять файлы из разных путей")
    @ParameterizedTest
    @MethodSource("filenameProvider")
    void shouldDeleteFilesFromDifferentPathsTest(String filename) throws IOException {
        byte[] content = "content".getBytes();
        Path filePath = baseUploadPath.resolve(filename);

        createTestFile(filePath, content);
        when(pathResolver.resolveFilePath(filename)).thenReturn(filePath);

        fileStorageService.deleteImageFile(filename);

        assertFalse(fileExists(filePath));
    }
}
