package io.github.habatoo.service.impl;

import io.github.habatoo.dto.request.CommentCreateRequestDto;
import io.github.habatoo.dto.request.CommentRequestDto;
import io.github.habatoo.dto.response.CommentResponseDto;
import io.github.habatoo.repositories.CommentRepository;
import io.github.habatoo.service.CommentService;
import io.github.habatoo.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Сервис для работы с комментариями блога.
 * Предоставляет бизнес-логику для операций с комментариями к постам.
 *
 * <p>Сервис делегирует выполнение операций доступа к данным репозиторию
 * и обеспечивает работу с комментариями, связанных с конкретными постами.</p>
 *
 * @see CommentRepository
 * @see PostServiceImpl
 */
@Slf4j
@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;

    public CommentServiceImpl(CommentRepository commentRepository, PostService postService) {
        this.commentRepository = commentRepository;
        this.postService = postService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        log.debug("Получение комментариев для поста id={}", postId);
        List<CommentResponseDto> loaded = commentRepository.findByPostId(postId);

        return new CopyOnWriteArrayList<>(loaded);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CommentResponseDto> getCommentByPostIdAndId(Long postId, Long commentId) {
        log.debug("Получение комментария id={} для поста id={}", commentId, postId);

        return commentRepository.findByPostIdAndId(postId, commentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentResponseDto createComment(CommentCreateRequestDto request) {
        log.info("Создание комментария для поста id={}", request.postId());
        Long postId = request.postId();

        try {
            CommentResponseDto newComment = commentRepository.save(request);
            postService.incrementCommentsCount(request.postId());
            log.info("Комментарий создан: id={}, postId={}", newComment.id(), postId);

            return newComment;
        } catch (Exception ex) {
            throw new IllegalStateException("Комментарий к посту id=" + postId + " не создан", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentResponseDto updateComment(CommentRequestDto commentRequest) {
        Long postId = commentRequest.postId();
        Long commentId = commentRequest.id();
        String text = commentRequest.text();
        log.info("Обновление комментария id={} для поста id={}", commentId, postId);

        CommentResponseDto updatedComment;
        try {
            updatedComment = commentRepository.update(postId, commentId, text);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Комментарий id={} не найден для обновления", commentId);
            throw new EmptyResultDataAccessException("Комментарий не найден", 1);
        }
        log.info("Комментарий обновлен: id={}, postId={}", updatedComment.id(), postId);

        return updatedComment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteComment(Long postId, Long commentId) {
        log.info("Удаление комментария id={} у поста id={}", commentId, postId);

        int deleted = commentRepository.deleteById(commentId);
        if (deleted > 0) {
            postService.decrementCommentsCount(postId);
            log.info("Комментарий удалён: id={}, postId={}", commentId, postId);
        } else {
            log.warn("Комментарий не найден для удаления: id={}, postId={}", commentId, postId);
            throw new EmptyResultDataAccessException("Комментарий не найден", 1);
        }
    }
}
