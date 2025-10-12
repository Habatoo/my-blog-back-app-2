package io.github.habatoo.service.impl;

import io.github.habatoo.service.ImageValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Валидатор для проверки изображений перед обработкой.
 * Выполняет проверку метаданных изображения.
 */
@Slf4j
@Component
public class ImageValidatorImpl implements ImageValidator {

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateImageUpdate(Long postId, MultipartFile image) {
        validatePostId(postId);
        if (image == null || image.isEmpty()) {
            log.warn("Попытка обновить изображение для postId={}, но файл отсутствует или пуст", postId);
            throw new IllegalStateException("Image file cannot be null or empty");
        }
        log.debug("Валидирован файл изображения для postId={} : originalName={}, size={}", postId, image.getOriginalFilename(), image.getSize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validatePostId(Long postId) {
        if (postId == null || postId <= 0) {
            log.warn("Некорректный postId при валидации: {}", postId);
            throw new IllegalStateException("Invalid postId: " + postId);
        }
        log.debug("Валидирован postId={}", postId);
    }
}
