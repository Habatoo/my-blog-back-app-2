package io.github.habatoo.service.filenamegenerator;

import io.github.habatoo.service.FileNameGenerator;
import io.github.habatoo.service.impl.FileNameGeneratorImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Тесты для приватного метода getFileExtension
 */
@DisplayName("Тесты метода getFileExtension")
class FileNameGeneratorGetFileExtensionTest extends FileNameGeneratorTestBase {

    @DisplayName("Должен извлекать расширение из файлов с различными форматами")
    @ParameterizedTest
    @MethodSource("fileExtensionProvider")
    void shouldExtractExtensionFromFilesWithExtensionsTest(String filename, String expectedExtension) {
        String result = ReflectionTestUtils.invokeMethod(fileNameGenerator, "getFileExtension", filename);

        assertEquals(expectedExtension, result);
    }

    @DisplayName("Должен возвращать расширение по умолчанию для файлов без расширения")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"file", "noextension", "justname"})
    void shouldReturnDefaultExtensionForFilesWithoutExtensionTest(String filename) {
        String result = ReflectionTestUtils.invokeMethod(fileNameGenerator, "getFileExtension", filename);

        assertEquals(DEFAULT_EXTENSION, result);
    }

    @DisplayName("Должен возвращать расширение по умолчанию для файлов с пустым расширением")
    @ParameterizedTest
    @ValueSource(strings = {"file.", "image.", "test."})
    void shouldReturnDefaultExtensionForEmptyExtensionTest(String filename) {
        String result = ReflectionTestUtils.invokeMethod(fileNameGenerator, "getFileExtension", filename);

        assertEquals(DEFAULT_EXTENSION, result);
    }

    @DisplayName("Должен обрабатывать файлы с несколькими точками")
    @ParameterizedTest
    @MethodSource("multipleDotsFileProvider")
    void shouldHandleFilesWithMultipleDotsTest(String filename, String expectedExtension) {
        String result = ReflectionTestUtils.invokeMethod(fileNameGenerator, "getFileExtension", filename);

        assertEquals(expectedExtension, result);
    }

    @DisplayName("Должен приводить расширения к нижнему регистру")
    @ParameterizedTest
    @MethodSource("uppercaseFileProvider")
    void shouldConvertExtensionToLowerCaseTest(String filename, String expectedExtension) {
        String result = ReflectionTestUtils.invokeMethod(fileNameGenerator, "getFileExtension", filename);

        assertEquals(expectedExtension, result);
    }

    @DisplayName("Должен обрабатывать граничные случаи с точками")
    @ParameterizedTest
    @MethodSource("edgeCasesFileProvider")
    void shouldHandleEdgeCasesWithDotsTest(String filename, String expectedExtension) {
        String result = ReflectionTestUtils.invokeMethod(fileNameGenerator, "getFileExtension", filename);

        assertEquals(expectedExtension, result);
    }

    @DisplayName("Должен работать с разными расширениями по умолчанию")
    @ParameterizedTest
    @MethodSource("defaultExtensionProvider")
    void shouldWorkWithDifferentDefaultExtensionsTest(String defaultExt) {
        when(imageProperties.defaultExtension()).thenReturn(defaultExt);
        FileNameGenerator generator = new FileNameGeneratorImpl(imageProperties);

        String result = ReflectionTestUtils.invokeMethod(generator, "getFileExtension", "file");

        assertEquals(defaultExt, result);
    }
}
