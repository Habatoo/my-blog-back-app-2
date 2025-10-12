package io.github.habatoo.service;

import io.github.habatoo.repository.CommentRepository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Интерфейс для обработки файлов
 *
 * @see CommentRepository
 * @see CommentService
 */
public interface FileStorageService {

    /**
     * Сохраняет файл изображения для указанного поста.
     *
     * @param postId идентификатор поста
     * @param file   файл изображения для сохранения
     * @return относительный путь к сохраненному файлу
     * @throws IOException при ошибках сохранения файла
     */
    String saveImageFile(Long postId, MultipartFile file) throws IOException;

    /**
     * Загружает файл изображения по имени файла.
     *
     * @param filename имя файла для загрузки
     * @return массив байт содержимого файла
     * @throws IOException при ошибках чтения файла
     */
    byte[] loadImageFile(String filename) throws IOException;

    /**
     * Удаляет файл изображения по имени файла.
     *
     * @param filename имя файла для удаления
     * @throws IOException при ошибках удаления файла
     */
    void deleteImageFile(String filename) throws IOException;

    /**
     * Удаляет директорию поста со всем содержимым.
     *
     * @param postId идентификатор поста
     */
    void deletePostDirectory(Long postId);

}
