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

    List<CommentResponse> getCommentsByPostId(Long postId);

    Optional<CommentResponse> getCommentByPostIdAndId(Long postId, Long commentId);

    CommentResponse createComment(CommentCreateRequest request);

    CommentResponse updateComment(Long postId, Long commentId, String text);

    void deleteComment(Long postId, Long commentId);
}

