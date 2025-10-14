package io.github.habatoo.repositories;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.repositories.impl.PostRepositoryImpl;
import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * Интерфейс репозитория для работы с постами блога.
 * Определяет контракты для операций доступа к данным постов.
 *
 * <p>Интерфейс расширяет Spring Data {@link Repository} и предоставляет
 * методы для извлечения постов вместе со связанными сущностями.</p>
 *
 * @see Repository
 * @see PostRepositoryImpl
 */
public interface PostRepository {

    /**
     * Получить список всех постов.
     *
     * @return список объектов PostResponse с информацией по всем постам
     */
    List<PostResponse> findAllPosts();

    /**
     * Создать новый пост.
     *
     * @param postCreateRequest объект с данными для создания поста: title, text, список тегов
     * @return созданный PostResponse с заполненными полями, включая сгенерированный id и список тегов
     * @throws IllegalStateException если пост не удалось создать
     */
    PostResponse createPost(PostCreateRequest postCreateRequest);

    /**
     * Обновить существующий пост.
     *
     * @param postRequest объект с данными для обновления поста: id, title, text, список тегов
     * @return обновлённый PostResponse с актуальными данными поста и тегов
     * @throws IllegalStateException если пост с указанным id не найден
     */
    PostResponse updatePost(PostRequest postRequest);

    /**
     * Удалить пост по идентификатору.
     *
     * @param id идентификатор удаляемого поста
     * @throws IllegalStateException если пост с указанным id не найден для удаления
     */
    void deletePost(Long id);

    /**
     * Увеличить счётчик лайков поста на 1.
     *
     * @param postId идентификатор поста для которого увеличивается количество лайков
     * @throws IllegalStateException если пост с указанным id не найден
     */
    void incrementLikes(Long postId);

    /**
     * Увеличить счётчик комментариев поста на 1.
     *
     * @param postId идентификатор поста для которого увеличивается количество комментариев
     */
    void incrementCommentsCount(Long postId);

    /**
     * Уменьшить счётчик комментариев поста на 1.
     *
     * @param postId идентификатор поста для которого уменьшается количество комментариев
     */
    void decrementCommentsCount(Long postId);

    /**
     * Получить список тэгов поста.
     *
     * @param postId идентификатор поста для которого уменьшается количество комментариев
     * @return обновлённый PostResponse с актуальными данными поста и тегов
     */
    List<String> getTagsForPost(Long postId);
}
