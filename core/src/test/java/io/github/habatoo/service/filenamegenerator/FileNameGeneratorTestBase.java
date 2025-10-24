package io.github.habatoo.service.filenamegenerator;

import io.github.habatoo.properties.ImageProperties;
import io.github.habatoo.service.FileNameGenerator;
import io.github.habatoo.service.impl.FileNameGeneratorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.mockito.Mockito.when;

/**
 * Базовый класс для тестирования FileNameGeneratorImpl
 */
@ExtendWith(MockitoExtension.class)
public abstract class FileNameGeneratorTestBase {


    protected FileNameGenerator fileNameGenerator;
    protected static final String DEFAULT_EXTENSION = "jpg";
    @Mock
    protected ImageProperties imageProperties;

    @BeforeEach
    void setUp() {
        when(imageProperties.defaultExtension()).thenReturn(DEFAULT_EXTENSION);
        fileNameGenerator = new FileNameGeneratorImpl(imageProperties);
    }

    protected String extractTimestamp(String filename) {
        return filename.split("_")[0];
    }

    protected String extractRandom(String filename) {
        return filename.split("_")[1].split("\\.")[0];
    }

    protected String extractExtension(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    protected boolean isValidTimestamp(String timestamp) {
        try {
            long ts = Long.parseLong(timestamp);
            return ts > 0 && ts <= System.currentTimeMillis();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    protected boolean isValidRandom(String random) {
        try {
            int rand = Integer.parseInt(random);
            return rand >= 1000 && rand <= 9999;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Провайдер данных для тестирования различных расширений файлов
     */
    protected static Stream<Arguments> fileExtensionProvider() {
        return Stream.of(
                Arguments.of("image.jpg", "jpg"),
                Arguments.of("photo.png", "png"),
                Arguments.of("picture.gif", "gif"),
                Arguments.of("document.pdf", "pdf"),
                Arguments.of("file.JPEG", "jpeg"),
                Arguments.of("IMAGE.PNG", "png"),
                Arguments.of("archive.zip", "zip"),
                Arguments.of("text.txt", "txt")
        );
    }

    /**
     * Провайдер данных для тестирования файлов с несколькими точками
     */
    protected static Stream<Arguments> multipleDotsFileProvider() {
        return Stream.of(
                Arguments.of("my.image.jpg", "jpg"),
                Arguments.of("file.name.with.dots.png", "png"),
                Arguments.of("version.1.2.3.tar.gz", "gz")
        );
    }

    /**
     * Провайдер данных для тестирования файлов в верхнем регистре
     */
    protected static Stream<Arguments> uppercaseFileProvider() {
        return Stream.of(
                Arguments.of("IMAGE.JPG", "jpg"),
                Arguments.of("Photo.PNG", "png"),
                Arguments.of("PICTURE.Gif", "gif"),
                Arguments.of("Document.JPEG", "jpeg"),
                Arguments.of("FILE.JPG", "jpg")
        );
    }

    /**
     * Провайдер данных для тестирования граничных случаев
     */
    protected static Stream<Arguments> edgeCasesFileProvider() {
        return Stream.of(
                Arguments.of(".", "jpg"),
                Arguments.of("..", "jpg"),
                Arguments.of("...", "jpg")
        );
    }

    /**
     * Провайдер данных для тестирования специальных символов
     */
    protected static Stream<Arguments> specialCharactersFileProvider() {
        return Stream.of(
                Arguments.of("file with spaces.jpg", "jpg"),
                Arguments.of("file-with-dashes.png", "png"),
                Arguments.of("file_with_underscores.gif", "gif"),
                Arguments.of("file(mixed).jpg", "jpg"),
                Arguments.of("file@special#chars.png", "png")
        );
    }

    /**
     * Провайдер данных для тестирования разных расширений по умолчанию
     */
    protected static Stream<Arguments> defaultExtensionProvider() {
        return Stream.of(
                Arguments.of("png"),
                Arguments.of("gif"),
                Arguments.of("webp"),
                Arguments.of("bmp")
        );
    }
}
