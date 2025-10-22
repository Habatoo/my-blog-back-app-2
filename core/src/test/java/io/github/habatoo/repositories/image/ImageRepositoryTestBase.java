package io.github.habatoo.repositories.image;

import io.github.habatoo.repositories.ImageRepository;
import io.github.habatoo.repositories.impl.ImageRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Базовый класс для тестирования ImageRepositoryImpl.
 */
@ExtendWith(MockitoExtension.class)
abstract class ImageRepositoryTestBase {

    @Mock
    protected JdbcTemplate jdbcTemplate;

    protected ImageRepository imageRepository;

    protected final Long EXISTING_POST_ID = 1L;
    protected final Long NON_EXISTING_POST_ID = 999L;
    protected final String IMAGE_NAME = "image_file.jpg";
    protected final long IMAGE_SIZE = 22L;
    protected final String URL = String.format("%s/%s", EXISTING_POST_ID, IMAGE_NAME);

    @BeforeEach
    void setUp() {
        imageRepository = new ImageRepositoryImpl(jdbcTemplate);
    }
}
