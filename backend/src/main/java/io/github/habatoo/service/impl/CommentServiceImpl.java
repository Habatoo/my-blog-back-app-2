package io.github.habatoo.service.impl;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.repository.CommentRepository;
import io.github.habatoo.service.CommentService;
import io.github.habatoo.service.PostService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final Map<Long, CopyOnWriteArrayList<CommentResponse>> commentsCache = new ConcurrentHashMap<>();

    public CommentServiceImpl(CommentRepository commentRepository, PostService postService) {
        this.commentRepository = commentRepository;
        this.postService = postService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        checkPostIsExist(postId);
        return commentsCache.computeIfAbsent(postId, id -> {
            return new CopyOnWriteArrayList<>(commentRepository.findByPostId(id));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CommentResponse> getCommentByPostIdAndId(Long postId, Long commentId) {
        CopyOnWriteArrayList<CommentResponse> comments = commentsCache.get(postId);
        if (comments != null) {
            return comments.stream()
                    .filter(c -> c.id().equals(commentId))
                    .findFirst();
        }
        return commentRepository.findByPostIdAndId(postId, commentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentResponse createComment(CommentCreateRequest request) {
        checkPostIsExist(request.postId());
        CommentResponse newComment = commentRepository.save(request);
        postService.incrementCommentsCount(request.postId());

        commentsCache.compute(request.postId(), (postId, comments) -> {
            if (comments == null) {
                comments = new CopyOnWriteArrayList<>();
            }
            comments.add(newComment);
            return comments;
        });
        return newComment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentResponse updateComment(Long postId, Long commentId, String text) { //TODO not update
        checkPostIsExist(postId);
        CommentResponse updatedComment;
        try {
            updatedComment = commentRepository.updateText(commentId, text);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalStateException("Comment not found for update with id " + commentId, e);
        }

        commentsCache.computeIfPresent(postId, (pid, comments) -> {
            comments.removeIf(c -> c.id().equals(commentId));
            comments.add(updatedComment);
            return comments;
        });
        return updatedComment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteComment(Long postId, Long commentId) {
        checkPostIsExist(postId);
        int deleted = commentRepository.deleteById(commentId);
        if (deleted > 0) {
            postService.decrementCommentsCount(postId);
            commentsCache.computeIfPresent(postId, (pid, comments) -> {
                comments.removeIf(c -> c.id().equals(commentId));
                return comments;
            });
        } else {
            throw new EmptyResultDataAccessException("Comment not found", 1);
        }
    }

    private void checkPostIsExist(Long postId) {
        if (!postService.postExists(postId)) {
            throw new IllegalStateException("Post not found with id " + postId);
        }
    }
}

