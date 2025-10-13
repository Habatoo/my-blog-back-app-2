package io.github.habatoo.service;

import io.github.habatoo.repositories.CommentRepository;

/**
 * Интерфейс для генерации уникальных имена файлов.
 *
 * @see CommentRepository
 * @see CommentService
 */
public interface FileNameGenerator {

    /**
     * Генерирует уникальное имя файла на основе оригинального имени.
     * Гарантирует уникальность через временную метку и безопасность через валидацию расширения.
     *
     * @param originalFilename оригинальное имя файла
     * @return сгенерированное уникальное имя файла
     */
    String generateFileName(String originalFilename);

}
