package io.github.habatoo.service.filestorage;

import io.github.habatoo.service.FileNameGenerator;
import io.github.habatoo.service.FileStorageService;
import io.github.habatoo.service.PathResolver;
import io.github.habatoo.service.impl.FileStorageServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

/**
 * Базовый класс для тестирования FileStorageServiceImpl
 */
@ExtendWith(MockitoExtension.class)
public abstract class FileStorageServiceTestBase {

    protected static final Long VALID_POST_ID = 1L;
    protected static final String TEST_UPLOAD_DIR = "test-uploads";

    protected FileStorageService fileStorageService;
    protected FileNameGenerator fileNameGenerator;
    protected PathResolver pathResolver;
    protected Path baseUploadPath;
    protected MockedStatic<Files> filesMock;

    @BeforeEach
    void setUp() {
        fileNameGenerator = mock(FileNameGenerator.class);
        pathResolver = mock(PathResolver.class);

        baseUploadPath = Paths.get(TEST_UPLOAD_DIR).toAbsolutePath().normalize();
        fileStorageService = new FileStorageServiceImpl(
                TEST_UPLOAD_DIR,
                false,
                fileNameGenerator,
                pathResolver
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(baseUploadPath)) {
            Files.walk(baseUploadPath)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                        }
                    });
        }
        if (filesMock != null) {
            filesMock.close();
        }
    }

    protected static Stream<Arguments> postIdProvider() {
        return Stream.of(
                Arguments.of(1L),
                Arguments.of(100L),
                Arguments.of(9999L)
        );
    }

    protected static Stream<Arguments> filenameProvider() {
        return Stream.of(
                Arguments.of("123/image1.jpg"),
                Arguments.of("456/nested/image2.png"),
                Arguments.of("789/file3.gif")
        );
    }

    protected static MockMultipartFile getFile(String filename, byte[] fileContent) {
        return new MockMultipartFile("image", filename, "image/jpeg", fileContent);
    }

    protected MultipartFile createMockMultipartFile(String filename, byte[] content) throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        lenient().when(file.getOriginalFilename()).thenReturn(filename);
        lenient().when(file.getBytes()).thenReturn(content);
        lenient().when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        return file;
    }

    protected void createTestFile(Path filePath, byte[] content) throws IOException {
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, content);
    }

    protected boolean fileExists(Path filePath) {
        return Files.exists(filePath);
    }

    protected boolean directoryExists(Path dirPath) {
        return Files.exists(dirPath) && Files.isDirectory(dirPath);
    }
}
