package io.github.habatoo.service;

import io.github.habatoo.repositories.CommentRepository;
import org.springframework.web.multipart.MultipartFile;

/**
 * Интерфейс для проверки изображений перед обработкой.
 *
 * @see CommentRepository
 * @see CommentService
 */
public interface ImageValidator {

    void validateImageUpdate(Long postId, MultipartFile image);

    void validatePostId(Long postId);

}
