package io.github.habatoo.service.filestorage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;

/**
 * Тесты для метода deletePostDirectory в FileStorageServiceImpl.
 *
 * <p>Покрывает логику полного и корректного удаления директорий постов:
 * <ul>
 *   <li>Удаление директорий с файлами и поддиректориями</li>
 *   <li>Удаление разных директорий по параметризованным id</li>
 *   <li>Игнорирование удаления, если директории не существует</li>
 *   <li>Работу с удалением пустых директорий</li>
 *   <li>Обработку ошибок: выброс RuntimeException при некорректном удалении</li>
 *   <li>Проверку веток exists/isDirectory и обработку исключений в статических вызовах Files</li>
 * </ul>
 * </p>
 */
@DisplayName("Тесты метода deletePostDirectory")
class FileStorageServiceDeletePostDirectoryTest extends FileStorageServiceTestBase {

    /**
     * Проверяет удаление директории поста вместе со всеми вложенными файлами и поддиректориями.
     */
    @Test
    @DisplayName("Должен удалить директорию поста со всеми файлами")
    void shouldDeletePostDirectoryWithAllFilesTest() throws IOException {
        Path postDir = baseUploadPath.resolve(VALID_POST_ID.toString());
        Path file1 = postDir.resolve("image1.jpg");
        Path file2 = postDir.resolve("image2.png");
        Path subDir = postDir.resolve("nested");
        Path file3 = subDir.resolve("image3.gif");

        createTestFile(file1, "content1".getBytes());
        createTestFile(file2, "content2".getBytes());
        createTestFile(file3, "content3".getBytes());

        fileStorageService.deletePostDirectory(VALID_POST_ID);

        assertFalse(directoryExists(postDir));
        assertFalse(fileExists(file1));
        assertFalse(fileExists(file2));
        assertFalse(fileExists(file3));
    }

    /**
     * Проверяет, что метод корректно удаляет директории для разных postId.
     */
    @DisplayName("Должен удалять директории разных постов")
    @ParameterizedTest
    @MethodSource("postIdProvider")
    void shouldDeleteDifferentPostDirectoriesTest(Long postId) throws IOException {
        Path postDir = baseUploadPath.resolve(postId.toString());
        Path file = postDir.resolve("test.jpg");

        createTestFile(file, "content".getBytes());

        fileStorageService.deletePostDirectory(postId);

        assertFalse(directoryExists(postDir));
    }

    /**
     * Проверяет, что попытка удалить несуществующую директорию не приводит к ошибке.
     */
    @Test
    @DisplayName("Должен игнорировать удаление несуществующей директории")
    void shouldIgnoreDeletingNonExistentDirectoryTest() {
        assertDoesNotThrow(() -> fileStorageService.deletePostDirectory(999L));
    }

    /**
     * Проверяет корректное удаление пустых директорий.
     */
    @Test
    @DisplayName("Должен удалять пустые директории")
    void shouldDeleteEmptyDirectoriesTest() throws IOException {
        Path postDir = baseUploadPath.resolve(VALID_POST_ID.toString());
        Files.createDirectories(postDir);

        fileStorageService.deletePostDirectory(VALID_POST_ID);

        assertFalse(directoryExists(postDir));
    }

    /**
     * Проверяет, что выбрасыватся RuntimeException,
     * если удаление любого файла внутри директории завершилось ошибкой.
     */
    @Test
    @DisplayName("Тест удаления любого файла внутри директории завершилось ошибкой")
    void shouldThrowIfDeleteFailsInsideWalkTest() {
        Path postDir = baseUploadPath.resolve(VALID_POST_ID.toString());
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(postDir)).thenReturn(true);
            filesMock.when(() -> Files.isDirectory(postDir)).thenReturn(true);
            filesMock.when(() -> Files.walk(postDir)).thenReturn(Stream.of(postDir));

            filesMock.when(() -> Files.delete(any())).thenThrow(new IOException("walk error"));
            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> fileStorageService.deletePostDirectory(VALID_POST_ID));
            assertTrue(ex.getMessage().contains("Failed to delete:"));
        }
    }

    /**
     * Проверяет, что выбрасыватся RuntimeException,
     * если walk завершился с ошибкой.
     */
    @Test
    @DisplayName("Должен выбрасывать RuntimeException если walk завершился с ошибкой")
    void shouldThrowOnWalkErrorTest() {
        Path postDir = baseUploadPath.resolve(VALID_POST_ID.toString());
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(postDir)).thenReturn(true);
            filesMock.when(() -> Files.isDirectory(postDir)).thenReturn(true);
            filesMock.when(() -> Files.walk(postDir)).thenThrow(new IOException("delete error"));
            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> fileStorageService.deletePostDirectory(VALID_POST_ID));
            assertTrue(ex.getMessage().contains("Error deleting post directory: delete error"));
        }
    }

    /**
     * Проверяет, что разные варианты Files.exists && Files.isDirectory не приводит к ошибке.
     */
    @ParameterizedTest(name = "exists={0}, isDirectory={1}")
    @CsvSource({
            "true,true",
            "false,false",
            "false,true",
            "true,false"
    })
    @DisplayName("Проверка: ветка Files.exists && Files.isDirectory должна срабатывать")
    void shouldCheckDifferentCombinationsTest(boolean exists, boolean isDirectory) {
        Long id = 2L;
        Path postDir = baseUploadPath.resolve(id.toString());
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(postDir)).thenReturn(exists);
            filesMock.when(() -> Files.isDirectory(postDir)).thenReturn(isDirectory);
            filesMock.when(() -> Files.deleteIfExists(postDir)).thenReturn(true);

            assertDoesNotThrow(() -> fileStorageService.deletePostDirectory(id));
        }
    }
}
