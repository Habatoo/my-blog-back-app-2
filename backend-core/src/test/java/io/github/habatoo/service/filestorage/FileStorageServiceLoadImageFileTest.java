package io.github.habatoo.service.filestorage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Тесты для метода loadImageFile.
 */
@DisplayName("Тесты метода loadImageFile")
class FileStorageServiceLoadImageFileTest extends FileStorageServiceTestBase {

    @Test
    @DisplayName("Должен загрузить содержимое файла изображения")
    void shouldLoadImageFileContentTest() throws IOException {
        String filename = "123/test.jpg";
        byte[] expectedContent = "image content".getBytes();
        Path filePath = baseUploadPath.resolve(filename);

        createTestFile(filePath, expectedContent);
        when(pathResolver.resolveFilePath(filename)).thenReturn(filePath);

        byte[] result = fileStorageService.loadImageFile(filename);

        assertArrayEquals(expectedContent, result);
    }

    @DisplayName("Должен загрузить файлы из разных путей")
    @ParameterizedTest
    @MethodSource("filenameProvider")
    void shouldLoadFilesFromDifferentPathsTest(String filename) throws IOException {
        byte[] content = "test content".getBytes();
        Path filePath = baseUploadPath.resolve(filename);

        createTestFile(filePath, content);
        when(pathResolver.resolveFilePath(filename)).thenReturn(filePath);

        byte[] result = fileStorageService.loadImageFile(filename);

        assertArrayEquals(content, result);
    }

    @Test
    @DisplayName("Должен выбросить исключение при загрузке несуществующего файла")
    void shouldThrowExceptionWhenLoadingNonExistentFileTest() {
        String filename = "nonexistent/file.jpg";
        Path filePath = baseUploadPath.resolve(filename);

        when(pathResolver.resolveFilePath(filename)).thenReturn(filePath);

        assertThrows(IOException.class, () -> fileStorageService.loadImageFile(filename));
    }
}
