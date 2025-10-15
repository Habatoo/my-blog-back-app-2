package io.github.habatoo.service;

import io.github.habatoo.dto.request.PostCreateRequestDto;
import io.github.habatoo.dto.request.PostRequestDto;
import io.github.habatoo.dto.response.PostListResponseDto;
import io.github.habatoo.dto.response.PostResponseDto;

import java.util.Optional;

/**
 * Интерфейс для работы с постами блога.
 * Предоставляет бизнес-логику для операций с постами.
 *
 * @see PostRepository
 * @see PostResponse
 */
/**
 * Сервис для работы с постами.
 */
public interface PostService {

    /**
     * Получить список постов с возможностью поиска и постраничной навигации.
     *
     * @param search строка для поиска по содержимому постов
     * @param pageNumber номер страницы для пагинации (начиная с 0)
     * @param pageSize количество постов на странице
     * @return объект PostListResponseDto, содержащий список постов и информацию о пагинации
     */
    PostListResponseDto getPosts(String search, int pageNumber, int pageSize);

    /**
     * Получить пост по его уникальному идентификатору.
     *
     * @param id идентификатор поста
     * @return Optional с PostResponseDto, если пост найден, иначе пустой Optional
     */
    Optional<PostResponseDto> getPostById(Long id);

    /**
     * Создать новый пост.
     *
     * @param postCreateRequest объект с данными для создания поста: title, text, теги и др.
     * @return созданный PostResponseDto с сгенерированным ID и другими заполненными полями
     * @throws IllegalStateException если не удалось создать пост
     */
    PostResponseDto createPost(PostCreateRequestDto postCreateRequest);

    /**
     * Обновить существующий пост.
     *
     * @param postRequest объект с данными для обновления поста: id, title, text, теги и др.
     * @return обновлённый PostResponseDto с актуальными данными
     * @throws IllegalStateException если пост с указанным ID не найден
     */
    PostResponseDto updatePost(PostRequestDto postRequest);

    /**
     * Удалить пост по его ID.
     *
     * @param id идентификатор поста для удаления
     * @throws IllegalStateException если пост не найден для удаления
     */
    void deletePost(Long id);

    /**
     * Увеличить счётчик лайков поста на 1.
     *
     * @param id идентификатор поста
     * @return обновлённое значение счётчика лайков
     * @throws IllegalStateException если пост не найден
     */
    int incrementLikes(Long id);

    /**
     * Увеличить счётчик комментариев поста на 1.
     *
     * @param id идентификатор поста
     */
    void incrementCommentsCount(Long id);

    /**
     * Уменьшить счётчик комментариев поста на 1.
     *
     * @param id идентификатор поста
     */
    void decrementCommentsCount(Long id);

    /**
     * Проверить существование поста с указанным ID.
     *
     * @param postId идентификатор поста
     * @return true если пост существует, иначе false
     */
    boolean postExists(Long postId);
}
