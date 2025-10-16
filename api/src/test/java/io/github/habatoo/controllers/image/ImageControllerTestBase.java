package io.github.habatoo.controllers.image;

import io.github.habatoo.controllers.ImageController;
import io.github.habatoo.service.ImageService;
import io.github.habatoo.service.dto.ImageResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

/**
 * Настройки тестов на покрытие основных сценариев работы контроллера изображений,
 * включая успешные операции, обработку различных форматов изображений,
 * граничные значения параметров и специфичные требования к
 * форматам multipart/form-data для загрузки и получения изображений.
 */
@ExtendWith(MockitoExtension.class)
public abstract class ImageControllerTestBase {

    protected static final Long VALID_POST_ID = 1L;
    protected static final Long NON_EXISTENT_POST_ID = 999L;

    @Mock
    protected ImageService imageService;

    @Mock
    protected MultipartFile multipartFile;

    protected ImageController imageController;

    @BeforeEach
    void setUp() {
        imageController = new ImageController(imageService);
    }

    protected ImageResponseDto createImageResponse(byte[] data, MediaType mediaType) {
        return new ImageResponseDto(data, mediaType);
    }

    protected byte[] createJpegImageData() {
        return new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x01, 0x02};
    }

    protected byte[] createPngImageData() {
        return new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    }

    protected byte[] createEmptyImageData() {
        return new byte[0];
    }
}
