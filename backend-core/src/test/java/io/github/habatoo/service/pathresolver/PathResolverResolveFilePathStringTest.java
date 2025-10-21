package io.github.habatoo.service.pathresolver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты метода resolveFilePath(String) PostServiceImpl.
 */
@DisplayName("Тесты PathResolverImpl.resolveFilePath(String)")
@ExtendWith(MockitoExtension.class)
class PathResolverResolveFilePathStringTest extends PathResolverTestBase {

    @ParameterizedTest(name = "Разрешить путь: {0}")
    @ValueSource(strings = {
            "image.jpg",
            "post123/image.png",
            "nested/dir/file.txt",
            "./image.jpg"
    })
    void shouldResolveFilePathWithinBaseTest(String filename) {
        Path resolved = pathResolver.resolveFilePath(filename);
        assertTrue(resolved.toAbsolutePath().startsWith(Paths.get(BASE_UPLOAD_DIR).toAbsolutePath()),
                "Путь должен быть внутри базового каталога");
    }

    @ParameterizedTest(name = "Запретить путь: {0}")
    @ValueSource(strings = {
            "../../outside.jpg",
            "/absolute/path/image.jpg",
            "/etc/passwd",
            "../outside_dir/image.png"
    })
    void shouldThrowSecurityExceptionForPathsOutsideBaseTest(String filename) {
        SecurityException ex = assertThrows(SecurityException.class,
                () -> pathResolver.resolveFilePath(filename));
        assertTrue(ex.getMessage().contains("Attempt to access file outside upload directory"));
    }
}
