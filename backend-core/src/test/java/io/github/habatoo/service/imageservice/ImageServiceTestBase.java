package io.github.habatoo.service.imageservice;

import io.github.habatoo.repositories.ImageRepository;
import io.github.habatoo.service.FileStorageService;
import io.github.habatoo.service.ImageContentTypeDetector;
import io.github.habatoo.service.ImageValidator;
import io.github.habatoo.service.impl.ImageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/**
 * Базовый класс для тестирования ImageServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
abstract class ImageServiceTestBase {

    @Mock
    protected ImageRepository imageRepository;

    @Mock
    protected FileStorageService fileStorageService;

    @Mock
    protected ImageValidator imageValidator;

    @Mock
    protected ImageContentTypeDetector contentTypeDetector;

    protected ImageServiceImpl imageService;

    protected static final Long VALID_POST_ID = 1L;
    protected static final Long INVALID_POST_ID = 999L;
    protected static final String IMAGE_FILENAME = "stored_image.jpg";
    protected static final String ORIGINAL_FILENAME = "original_image.jpg";
    protected static final String URL = String.format("%s%s%s", VALID_POST_ID, System.getProperty("file.separator"), IMAGE_FILENAME);
    protected static final long IMAGE_SIZE = 123456L;

    protected static final byte[] IMAGE_DATA = new byte[]{1, 2, 3, 4, 5};
    protected static final MediaType MEDIA_TYPE = MediaType.IMAGE_JPEG;

    @BeforeEach
    void setUp() {
        imageService = new ImageServiceImpl(
                imageRepository,
                fileStorageService,
                imageValidator,
                contentTypeDetector
        );
    }

    protected MultipartFile createMultipartFile(boolean empty, String originalFilename, long size) {
        MultipartFile file = mock(MultipartFile.class);
        lenient().when(file.isEmpty()).thenReturn(empty);
        lenient().when(file.getOriginalFilename()).thenReturn(originalFilename);
        lenient().when(file.getSize()).thenReturn(size);
        return file;
    }
}
