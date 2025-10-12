package io.github.habatoo.service.filestorage;

import io.github.habatoo.service.impl.FileStorageServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Тесты для метода saveImageFile в FileStorageServiceImpl,
 * покрывающие различные крайние кейсы и сценарии работы с файловой системой.
 *
 * <p>Включает:
 * <ul>
 *   <li>Автоматическое создание директории при включенной соответствующей опции</li>
 *   <li>Обработку исключений при невозможности создать директорию</li>
 *   <li>Работу с очень длинными именами файлов</li>
 *   <li>Обработку специальных символов в именах файлов</li>
 *   <li>Параллельное сохранение нескольких файлов с генерацией уникальных имён</li>
 * </ul>
 * </p>
 */
@DisplayName("Тесты метода saveImageFile")
class FileStorageServiceEdgeCasesTest extends FileStorageServiceTestBase {

    /**
     * Проверяет автоматическое создание директории загрузки при наличии опции autoCreateDir.
     * После теста загрузочная директория удаляется рекурсивно для предотвращения побочных эффектов.
     */
    @Test
    @DisplayName("Должен автоматически создавать upload директорию при включенной опции")
    void shouldAutoCreateUploadDirectoryWhenEnabledTest() {
        Path customUploadDir = Paths.get("auto-create-test");

        new FileStorageServiceImpl(
                "auto-create-test",
                true,
                fileNameGenerator,
                pathResolver
        );

        assertTrue(directoryExists(customUploadDir));

        try {
            Files.walk(customUploadDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignore) {
                        }
                    });
        } catch (IOException ignore) {
        }
    }

    /**
     * Проверяет, что конструктор выбрасывает RuntimeException,
     * если директория для загрузки не может быть создана из-за ошибки ввода-вывода.
     */
    @Test
    @DisplayName("Должен выбросить RuntimeException если директория не создаётся при инициализации")
    void shouldThrowIfCannotCreateUploadDirTest() {
        Path badPath = Paths.get("/bad/path/to/uploads").toAbsolutePath().normalize();

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(badPath)).thenReturn(false);
            filesMock.when(() -> Files.createDirectories(badPath))
                    .thenThrow(new IOException("Cannot create dir"));

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    new FileStorageServiceImpl(
                            badPath.toString(),
                            true,
                            fileNameGenerator,
                            pathResolver
                    )
            );

            assertTrue(ex.getMessage().contains("Failed to create upload directory"));
            assertTrue(ex.getCause() instanceof IOException);
        }
    }

    /**
     * Проверяет, что если целевой путь уже существует,
     * но не соответствует требованиям (например, не директория или недоступна для записи),
     * конструктор выбрасывает корректный RuntimeException.
     */
    @Test
    @DisplayName("Должен выбросить RuntimeException если файла не существует при инициализации")
    void shouldThrowIfCannotCreateFileTest() {
        Path badPath = Paths.get("/bad/path/to/uploads").toAbsolutePath().normalize();

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(badPath)).thenReturn(true);
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    new FileStorageServiceImpl(
                            badPath.toString(),
                            true,
                            fileNameGenerator,
                            pathResolver
                    )
            );

            assertTrue(ex.getMessage().contains("Upload directory is not accessible for writing"));
            assertTrue(ex.toString().contains("RuntimeException"));
        }
    }

    /**
     * Проверяет корректную работу с очень длинными именами файлов:
     * убедившись, что файл успешно создаётся на диске и результат не null.
     */
    @Test
    @DisplayName("Должен обрабатывать очень длинные имена файлов")
    void shouldHandleVeryLongFilenamesTest() throws IOException {
        String longFilename = "a".repeat(100) + ".jpg";
        String generatedName = "12345_6789.jpg";
        byte[] content = "content".getBytes();
        MultipartFile file = getFile(longFilename, content);
        Path postDir = baseUploadPath.resolve(VALID_POST_ID.toString());
        Path filePath = postDir.resolve(generatedName);

        when(fileNameGenerator.generateFileName(longFilename)).thenReturn(generatedName);
        when(pathResolver.resolveFilePath(postDir, generatedName)).thenReturn(filePath);

        String result = fileStorageService.saveImageFile(VALID_POST_ID, file);

        assertNotNull(result);
        assertTrue(fileExists(filePath));
    }

    /**
     * Проверяет сохранение изображений с именами, содержащими пробелы и специальные символы,
     * и убеждается, что файл корректно сохраняется по ожидаемому пути.
     */
    @Test
    @DisplayName("Должен обрабатывать специальные символы в именах файлов")
    void shouldHandleSpecialCharactersInFilenamesTest() throws IOException {
        String specialFilename = "file with spaces and (special) chars.jpg";
        String generatedName = "12345_6789.jpg";
        byte[] content = "content".getBytes();
        MultipartFile file = getFile(specialFilename, content);
        Path postDir = baseUploadPath.resolve(VALID_POST_ID.toString());
        Path filePath = postDir.resolve(generatedName);

        when(fileNameGenerator.generateFileName(specialFilename)).thenReturn(generatedName);
        when(pathResolver.resolveFilePath(postDir, generatedName)).thenReturn(filePath);

        String result = fileStorageService.saveImageFile(VALID_POST_ID, file);

        assertNotNull(result);
        assertTrue(fileExists(filePath));
    }

    /**
     * Проверяет поддержку параллельной загрузки файлов:
     * все параллельные операции должны корректно завершиться, а файлы — существовать после выполнения.
     */
    @Test
    @DisplayName("Должен обрабатывать параллельные операции с файлами")
    void shouldHandleParallelFileOperationsTest() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Callable<String>> tasks = new ArrayList<>();

        // Генерирует уникальное имя файла на основе входного имени, например: "image2.jpg" -> "12345_2.jpg"
        when(fileNameGenerator.generateFileName(anyString()))
                .thenAnswer(invocation -> {
                    String original = invocation.getArgument(0);
                    // Извлекаем числовой индекс из имени файла "imageX.jpg"
                    String index = original.replace("image", "").replace(".jpg", "");
                    return "12345_" + index + ".jpg";
                });

        // Возвращает путь к файлу в папке поста
        when(pathResolver.resolveFilePath(any(Path.class), anyString()))
                .thenAnswer(invocation ->
                        ((Path) invocation.getArgument(0))
                                .resolve((String) invocation.getArgument(1))
                );


        for (int i = 0; i < 5; i++) {
            final int index = i;
            tasks.add(() -> {
                String filename = "image" + index + ".jpg";
                byte[] content = ("content" + index).getBytes();
                MultipartFile file = createMockMultipartFile(filename, content);

                return fileStorageService.saveImageFile(VALID_POST_ID, file);
            });
        }

        List<Future<String>> results = executor.invokeAll(tasks);
        executor.shutdown();

        for (Future<String> result : results) {
            assertNotNull(result.get());
        }

        assertTrue(directoryExists(baseUploadPath.resolve(VALID_POST_ID.toString())));
    }
}
