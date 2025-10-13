package io.github.habatoo.service.filestorage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Тесты для метода saveImageFile.
 */
@DisplayName("Тесты метода saveImageFile")
class FileStorageServiceSaveImageFileTest extends FileStorageServiceTestBase {

    @Test
    @DisplayName("Должен сохранить файл изображения и вернуть путь")
    void shouldSaveImageFileAndReturnPathTest() throws IOException {
        String filename = "test.jpg";
        String generatedName = "12345_6789.jpg";
        byte[] fileContent = "test image content".getBytes();
        MultipartFile file = getFile(filename, fileContent);
        Path postDir = baseUploadPath.resolve(VALID_POST_ID.toString());
        Path expectedFilePath = postDir.resolve(generatedName);

        when(fileNameGenerator.generateFileName(filename)).thenReturn(generatedName);
        when(pathResolver.resolveFilePath(postDir, generatedName)).thenReturn(expectedFilePath);

        String result = fileStorageService.saveImageFile(VALID_POST_ID, file);

        assertEquals(VALID_POST_ID + "/" + generatedName, result);
        assertTrue(fileExists(expectedFilePath));
    }

    @DisplayName("Должен сохранить файлы для разных постов")
    @ParameterizedTest
    @MethodSource("postIdProvider")
    void shouldSaveFilesForDifferentPostsTest(Long postId) throws IOException {
        String filename = "image.jpg";
        String generatedName = "timestamp_random.jpg";
        byte[] content = "content".getBytes();
        MultipartFile file = getFile(filename, content);
        Path postDir = baseUploadPath.resolve(postId.toString());
        Path filePath = postDir.resolve(generatedName);

        when(fileNameGenerator.generateFileName(filename)).thenReturn(generatedName);
        when(pathResolver.resolveFilePath(postDir, generatedName)).thenReturn(filePath);

        String result = fileStorageService.saveImageFile(postId, file);

        assertTrue(result.contains(postId.toString()));
        assertTrue(fileExists(filePath));
    }

    @Test
    @DisplayName("Должен создавать директорию поста при сохранении")
    void shouldCreatePostDirectoryWhenSavingTest() throws IOException {
        String filename = "test.png";
        String generatedName = "11111_2222.png";
        byte[] content = "png content".getBytes();
        MultipartFile file = createMockMultipartFile(filename, content);
        Path postDir = baseUploadPath.resolve(VALID_POST_ID.toString());
        Path filePath = postDir.resolve(generatedName);

        when(fileNameGenerator.generateFileName(filename)).thenReturn(generatedName);
        when(pathResolver.resolveFilePath(postDir, generatedName)).thenReturn(filePath);

        fileStorageService.saveImageFile(VALID_POST_ID, file);

        assertTrue(directoryExists(postDir));
    }

    @DisplayName("Должен обрабатывать разные типы файлов")
    @ParameterizedTest
    @ValueSource(strings = {"image.jpg", "photo.png", "picture.gif", "file.pdf"})
    void shouldHandleDifferentFileTypesTest(String filename) throws IOException {
        String generatedName = "12345_6789" + filename.substring(filename.lastIndexOf('.'));
        byte[] content = "file content".getBytes();
        MultipartFile file = createMockMultipartFile(filename, content);
        Path postDir = baseUploadPath.resolve(VALID_POST_ID.toString());
        Path filePath = postDir.resolve(generatedName);

        when(fileNameGenerator.generateFileName(filename)).thenReturn(generatedName);
        when(pathResolver.resolveFilePath(postDir, generatedName)).thenReturn(filePath);

        String result = fileStorageService.saveImageFile(VALID_POST_ID, file);

        assertTrue(result.endsWith(generatedName));
    }
}
