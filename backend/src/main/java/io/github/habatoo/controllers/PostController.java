package io.github.habatoo.controllers;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostListResponse;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для управления постами блога.
 *
 * <p>Предоставляет REST API endpoints для выполнения CRUD операций с постами.
 * Поддерживает создание, чтение, обновление и удаление постов, а также управление лайками.
 * Все endpoints возвращают данные в формате JSON.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    /**
     * Конструктор контроллера постов.
     *
     * @param postService сервис для бизнес-логики работы с постами
     */
    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * Получает пагинированный список постов с возможностью поиска.
     *
     * <p>Обрабатывает GET запросы для получения списка постов с поддержкой
     * пагинации и полнотекстового поиска по заголовку и содержимому постов.</p>
     *
     * @param search     строка для поиска по заголовку и содержимому постов
     * @param pageNumber номер страницы для пагинации (начинается с 1)
     * @param pageSize   количество постов на одной странице
     * @return список постов с метаданными пагинации
     * @throws IllegalArgumentException если параметры пагинации невалидны
     */
    @GetMapping
    public ResponseEntity<PostListResponse> getPosts(
            @RequestParam("search") String search,
            @RequestParam("pageNumber") int pageNumber,
            @RequestParam("pageSize") int pageSize) {
        log.info("Запрос на получение списка постов: search='{}', pageNumber={}, pageSize={}", search, pageNumber, pageSize);
        PostListResponse result = postService.getPosts(search, pageNumber, pageSize);
        return ResponseEntity.ok(result);
    }

    /**
     * Получает полную информацию о посте по идентификатору.
     *
     * <p>Возвращает подробную информацию о посте включая теги, количество лайков
     * и комментариев. Если пост не найден, возвращает статус 404 Not Found.</p>
     *
     * @param id идентификатор запрашиваемого поста
     * @return пост с полной информацией или 404 если не найден
     * @throws IllegalArgumentException если идентификатор невалиден
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable("id") Long id) {
        log.info("Запрос на получение поста по id={}", id);
        return postService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Создает новый пост в блоге.
     *
     * <p>Принимает данные для создания поста, валидирует их и сохраняет в системе.
     * Возвращает созданный пост с присвоенным идентификатором.</p>
     *
     * @param postCreateRequest данные для создания нового поста
     * @return созданный пост со статусом 201 Created
     * @throws IllegalArgumentException если данные запроса невалидны
     * @throws DataAccessException      при ошибках сохранения в базу данных
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody PostCreateRequest postCreateRequest) {
        log.info("Запрос на создание нового поста");
        PostResponse result = postService.createPost(postCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Обновляет существующий пост.
     *
     * <p>Обновляет заголовок, содержимое и теги указанного поста.
     * Если пост не найден, выбрасывает исключение.</p>
     *
     * @param id          идентификатор обновляемого поста
     * @param postRequest данные для обновления поста
     * @return обновленный пост
     * @throws IllegalArgumentException       если данные запроса невалидны
     * @throws EmptyResultDataAccessException если пост с указанным ID не найден
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable("id") Long id,
            @RequestBody PostRequest postRequest) {
        log.info("Запрос на обновление поста id={}", id);
        PostResponse result = postService.updatePost(postRequest);
        return ResponseEntity.ok(result);
    }

    /**
     * Удаляет пост по идентификатору.
     *
     * <p>Удаляет пост вместе со всеми связанными комментариями и тегами.
     * Если пост не найден, выбрасывает исключение.</p>
     *
     * @param id идентификатор удаляемого поста
     * @return пустой ответ со статусом 200 OK при успешном удалении
     * @throws IllegalArgumentException       если идентификатор невалиден
     * @throws EmptyResultDataAccessException если пост с указанным ID не найден
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") Long id) {
        log.info("Запрос на удаление поста id={}", id);
        postService.deletePost(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Увеличивает счетчик лайков поста на единицу.
     *
     * <p>Атомарно увеличивает количество лайков у указанного поста.
     * Возвращает обновленное количество лайков.</p>
     *
     * @param id идентификатор поста для увеличения лайков
     * @return обновленное количество лайков поста
     * @throws IllegalArgumentException       если идентификатор невалиден
     * @throws EmptyResultDataAccessException если пост с указанным ID не найден
     */
    @PostMapping("/{id}/likes")
    public ResponseEntity<Integer> incrementLikes(@PathVariable("id") Long id) {
        log.info("Запрос на увеличение лайков для поста id={}", id);
        int likesCount = postService.incrementLikes(id);
        return ResponseEntity.ok(likesCount);
    }
}
