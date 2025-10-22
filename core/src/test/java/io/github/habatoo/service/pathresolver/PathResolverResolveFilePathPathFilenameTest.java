package io.github.habatoo.service.pathresolver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты метода resolveFilePath(Path, String) PostServiceImpl.
 */
@DisplayName("Тесты PathResolverImpl.resolveFilePath(Path, String)")
@ExtendWith(MockitoExtension.class)
class PathResolverResolveFilePathPathFilenameTest extends PathResolverTestBase {

    @ParameterizedTest(name = "Разрешить файл в директории: {0} / {1}")
    @CsvSource({
            "uploads/posts/123, image.jpg",
            "uploads/posts/456, nested.png",
            "uploads/posts/100, ./file.txt"
    })
    void shouldResolveFilePathWithDirectoryWithinBaseTest(String dir, String filename) {
        Path dirPath = Paths.get(dir).toAbsolutePath().normalize();
        Path resolved = pathResolver.resolveFilePath(dirPath, filename);
        assertTrue(resolved.startsWith(dirPath),
                "Разрешённый файл должен быть внутри каталога");
        assertTrue(dirPath.startsWith(Paths.get(BASE_UPLOAD_DIR).toAbsolutePath()),
                "Директория должна быть внутри базового каталога");
    }

    @ParameterizedTest(name = "Запретить файл вне каталога: {0} / {1}")
    @CsvSource({
            "uploads/other_dir, image.jpg",
            "uploads/posts/123, ../../escape.jpg",
            "/etc, passwd",
            "'C:\\uploads\\posts\\123', image.jpg",
            "../outside_dir, image.png"
    })
    void shouldThrowSecurityExceptionForInvalidDirectoryOrFileTest(String dir, String filename) {
        Path dirPath = Paths.get(dir).toAbsolutePath().normalize();
        SecurityException ex = assertThrows(SecurityException.class,
                () -> pathResolver.resolveFilePath(dirPath, filename));
        assertTrue(ex.getMessage().contains("Attempt to access"));
    }
}
