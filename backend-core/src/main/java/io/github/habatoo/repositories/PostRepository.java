package io.github.habatoo.repositories;

import io.github.habatoo.dto.request.PostCreateRequestDto;
import io.github.habatoo.dto.request.PostRequestDto;
import io.github.habatoo.dto.response.PostResponseDto;
import io.github.habatoo.repositories.impl.PostRepositoryImpl;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

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
     * Получает список постов, удовлетворяющих строке поиска и/или фильтру по тегам,
     * с применением пагинации (ограничение по размеру страницы и смещению).
     *
     * @param searchPart строка поиска, фильтрует по заголовку или тексту поста (ILIKE)
     * @param tags       список тегов; если не пустой — искать только посты, содержащие указанные теги
     * @param pageNumber номер страницы (начиная с 1)
     * @param pageSize   количество постов на странице
     * @return список объектов PostResponseDto для отображения постов на заданной странице и с учётом поиска/тегов
     */
    List<PostResponseDto> findPosts(String searchPart, List<String> tags, int pageNumber, int pageSize);

    /**
     * Вычисляет количество всех постов в базе данных,
     * соответствующих фильтру поиска и указанным тегам (для расчёта количества страниц).
     *
     * @param searchPart строка поиска, фильтрует по заголовку или тексту поста (ILIKE)
     * @param tags       список тегов, которые должны быть у поста
     * @return общее количество постов, подходящих под фильтр
     */
    int countPosts(String searchPart, List<String> tags);

    /**
     * Получает пост по его идентификатору (id).
     *
     * @param postId уникальный идентификатор поста
     * @return объект PostResponseDto, если найдено; null или исключение, если не найдено
     */
    Optional<PostResponseDto> getPostById(Long postId);

    /**
     * Создать новый пост.
     *
     * @param postCreateRequest объект с данными для создания поста: title, text, список тегов
     * @return созданный PostResponseDto с заполненными полями, включая сгенерированный id и список тегов
     * @throws IllegalStateException если пост не удалось создать
     */
    PostResponseDto createPost(PostCreateRequestDto postCreateRequest);

    /**
     * Обновить существующий пост.
     *
     * @param postRequest объект с данными для обновления поста: id, title, text, список тегов
     * @return обновлённый PostResponseDto с актуальными данными поста и тегов
     * @throws IllegalStateException если пост с указанным id не найден
     */
    PostResponseDto updatePost(PostRequestDto postRequest);

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
     * @return обновлённый PostResponseDto с актуальными данными поста и тегов
     */
    List<String> getTagsForPost(Long postId);
}
