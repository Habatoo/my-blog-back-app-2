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

    /**
     * Проверяет корректность обновляемого изображения для заданного поста.
     *
     * @param postId идентификатор поста, для которого обновляется изображение
     * @param image  файл изображения для проверки
     * @throws IllegalArgumentException если изображение невалидно или не соответствует требованиям
     */
    void validateImageUpdate(Long postId, MultipartFile image);

    /**
     * Проверяет корректность идентификатора поста.
     *
     * @param postId идентификатор поста для проверки
     * @throws IllegalArgumentException если postId некорректен или пост не найден
     */
    void validatePostId(Long postId);
}
