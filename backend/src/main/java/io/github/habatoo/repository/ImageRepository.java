package io.github.habatoo.repository;

import io.github.habatoo.repository.impl.ImageRepositoryImpl;
import io.github.habatoo.service.FileStorageService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Интерфейс для работы с метаданными изображений постов.
 *
 * <p>Предоставляет методы для доступа и управления метаданными изображений,
 * связанных с постами в базе данных. Обеспечивает абстракцию над SQL-операциями
 * для работы с изображениями.</p>
 *
 * <p>Все методы работают только с метаданными изображений (имена файлов, размеры, ссылки),
 * сами операции с файлами изображений делегируются {@link FileStorageService}.</p>
 *
 * @see ImageRepositoryImpl
 * @see FileStorageService
 */
public interface ImageRepository {

    /**
     * Находит имя файла изображения для указанного поста.
     *
     * <p>Выполняет поиск в базе данных имени файла изображения, связанного с постом.
     * Если у поста нет изображения, возвращает пустой Optional.</p>
     *
     * @param postId идентификатор поста для поиска изображения
     * @return Optional с именем файла изображения, если найдено, иначе empty
     * @throws IllegalArgumentException если postId равен null или невалиден
     * @throws DataAccessException      при ошибках доступа к базе данных
     */
    Optional<String> findImageFileNameByPostId(Long postId);

    /**
     * Обновляет метаданные изображения для указанного поста.
     *
     * <p>Обновляет информацию об изображении в базе данных, включая имя файла,
     * оригинальное имя и размер. Выполняется в транзакции для обеспечения
     * целостности данных.</p>
     *
     * @param postId       идентификатор поста для обновления
     * @param fileName     имя сохраненного файла изображения
     * @param originalName оригинальное имя файла от пользователя
     * @param size         размер файла в байтах
     * @throws IllegalArgumentException       если любой из параметров невалиден
     * @throws EmptyResultDataAccessException если пост с указанным ID не найден
     * @throws DataAccessException            при ошибках обновления в базе данных
     */
    @Transactional
    void updateImageMetadata(Long postId, String fileName, String originalName, long size);

    /**
     * Проверяет существование поста по идентификатору.
     *
     * <p>Выполняет проверку наличия поста в базе данных по указанному идентификатору.
     * Используется для валидации перед операциями с изображениями.</p>
     *
     * @param postId идентификатор поста для проверки
     * @return true если пост существует, false в противном случае
     * @throws IllegalArgumentException если postId равен null или невалиден
     * @throws DataAccessException      при ошибках доступа к базе данных
     */
    boolean existsPostById(Long postId);

}
