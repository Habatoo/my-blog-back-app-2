package io.github.habatoo.service;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.repositories.CommentRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы с комментариями блога.
 * Предоставляет бизнес-логику для операций с комментариями к постам.
 *
 * <p>Делегирует выполнение операций доступа к данным репозиторию
 * и обеспечивает работу с комментариями, связанных с конкретными постами.</p>
 *
 * @see CommentRepository
 */
@Transactional
public interface CommentService {

    /**
     * Получить список комментариев для указанного поста.
     *
     * @param postId идентификатор поста, для которого запрашиваются комментарии
     * @return список CommentResponse с комментариями к посту; пустой список если комментариев нет
     */
    List<CommentResponse> getCommentsByPostId(Long postId);

    /**
     * Получить комментарий по идентификаторам поста и комментария.
     *
     * @param postId идентификатор поста, которому принадлежит комментарий
     * @param commentId идентификатор комментария
     * @return Optional с CommentResponse, либо пустой если комментарий не найден
     */
    Optional<CommentResponse> getCommentByPostIdAndId(Long postId, Long commentId);

    /**
     * Создать новый комментарий.
     *
     * @param request объект с данными для создания комментария: текст, id поста и пр.
     * @return созданный CommentResponse с заполненными полями, включая сгенерированный id
     * @throws IllegalStateException при ошибке создания комментария
     */
    CommentResponse createComment(CommentCreateRequest request);

    /**
     * Обновить текст существующего комментария.
     *
     * @param postId идентификатор поста, к которому относится комментарий
     * @param commentId идентификатор обновляемого комментария
     * @param text новый текст комментария
     * @return обновлённый CommentResponse
     * @throws IllegalStateException если комментарий с указанными id не найден
     */
    CommentResponse updateComment(Long postId, Long commentId, String text);

    /**
     * Удалить комментарий по идентификаторам поста и комментария.
     *
     * @param postId идентификатор поста, к которому относится комментарий
     * @param commentId идентификатор удаляемого комментария
     * @throws IllegalStateException если комментарий не найден для удаления
     */
    void deleteComment(Long postId, Long commentId);
}

