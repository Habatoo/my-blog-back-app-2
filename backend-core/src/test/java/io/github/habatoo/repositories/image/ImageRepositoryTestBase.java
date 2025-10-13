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

    protected final Long existingPostId = 1L;
    protected final Long nonExistingPostId = 999L;
    protected final String imageName = "image_file.jpg";

    @BeforeEach
    void setUp() {
        imageRepository = new ImageRepositoryImpl(jdbcTemplate);
    }
}
