package io.github.habatoo.service.impl;

import io.github.habatoo.service.ImageValidator;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Валидатор для проверки изображений перед обработкой.
 * Выполняет проверку метаданных изображения.
 */
@Component
public class ImageValidatorImpl implements ImageValidator {

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateImageUpdate(Long postId, MultipartFile image) {
        validatePostId(postId);
        if (image == null || image.isEmpty()) {
            throw new IllegalStateException("Image file cannot be null or empty");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validatePostId(Long postId) {
        if (postId == null || postId <= 0) {
            throw new IllegalStateException("Invalid postId: " + postId);
        }
    }
}

