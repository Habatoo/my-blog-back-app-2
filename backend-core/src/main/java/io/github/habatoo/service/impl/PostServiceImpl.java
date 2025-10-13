package io.github.habatoo.service.impl;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostListResponse;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.repositories.PostRepository;
import io.github.habatoo.service.FileStorageService;
import io.github.habatoo.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Сервис для работы с постами блога.
 * Предоставляет бизнес-логику для операций с постами.
 *
 * @see PostRepository
 * @see PostResponse
 * @see FileStorageService
 */
@Slf4j
@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final FileStorageService fileStorageService;

    private final Map<Long, PostResponse> postCache = new ConcurrentHashMap<>();

    public PostServiceImpl(
            PostRepository postRepository,
            FileStorageService fileStorageService
    ) {
        this.postRepository = postRepository;
        this.fileStorageService = fileStorageService;
        initCache();
    }

    @PostConstruct
    public void initCache() {
        log.info("Инициализация кеша постов из репозитория");
        List<PostResponse> allPosts = postRepository.findAllPosts();
        postCache.clear();
        allPosts.forEach(post -> postCache.put(post.id(), post));
        log.info("Кеш загружен, постов: {}", allPosts.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostListResponse getPosts(String search, int pageNumber, int pageSize) {
        log.debug("Запрошен список постов: search='{}', pageNumber={}, pageSize={}", search, pageNumber, pageSize);
        List<String> words = Arrays.stream(search.split("\\s+"))
                .filter(w -> !w.isBlank())
                .toList();

        List<String> tags = words.stream()
                .filter(w -> w.startsWith("#"))
                .map(w -> w.substring(1))
                .toList();

        String searchPart = words.stream()
                .filter(w -> !w.startsWith("#"))
                .collect(Collectors.joining(" "));

        List<PostResponse> filtered = postCache.values().stream()
                .filter(post -> searchPart.isEmpty()
                        || post.title().contains(searchPart)
                        || post.text().contains(searchPart))
                .filter(post -> tags.isEmpty()
                        || tags.stream().allMatch(tag ->
                        post.tags().stream().anyMatch(t -> t.equals(tag))
                ))
                .sorted(Comparator.comparing(PostResponse::id))
                .toList();

        int totalCount = filtered.size();
        int fromIndex = Math.min((pageNumber - 1) * pageSize, totalCount);
        int toIndex = Math.min(fromIndex + pageSize, totalCount);
        List<PostResponse> page = filtered.subList(fromIndex, toIndex);

        log.debug("Фильтровано {} постов, от {} до {}", totalCount, fromIndex, toIndex);

        return new PostListResponse(page, fromIndex > 0, toIndex < totalCount, totalCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PostResponse> getPostById(Long id) {
        log.debug("Получение поста по id={}", id);

        return Optional.ofNullable(postCache.get(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostResponse createPost(PostCreateRequest postCreateRequest) {
        log.info("Создание нового поста: title='{}'", postCreateRequest.title());

        try {
            PostResponse createdPost = postRepository.createPost(postCreateRequest);
            postCache.put(createdPost.id(), createdPost);
            log.info("Пост создан: id={}", createdPost.id());
            return createdPost;
        } catch (Exception e) {
            log.error("Не удалось создать пост: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to create post", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostResponse updatePost(PostRequest postRequest) {
        log.info("Обновление поста: id={}", postRequest.id());

        try {
            PostResponse updatedPost = postRepository.updatePost(postRequest);
            postCache.put(updatedPost.id(), updatedPost);
            log.info("Пост обновлён: id={}", updatedPost.id());
            return updatedPost;
        } catch (Exception e) {
            log.error("Ошибка при обновлении поста id={}: {}", postRequest.id(), e.getMessage(), e);
            throw new IllegalStateException("Post not found or concurrently modified with id " + postRequest.id(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePost(Long id) {
        log.info("Удаление поста id={}", id);
        postRepository.deletePost(id);
        postCache.remove(id);
        fileStorageService.deletePostDirectory(id);
        log.info("Пост и директория файлов удалены: id={}", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int incrementLikes(Long id) {
        log.debug("Инкремент лайков для поста id={}", id);
        postRepository.incrementLikes(id);
        PostResponse post = postCache.get(id);
        if (post != null) {
            int updatedLikes = post.likesCount() + 1;
            PostResponse updatedPost = new PostResponse(
                    post.id(),
                    post.title(),
                    post.text(),
                    post.tags(),
                    updatedLikes,
                    post.commentsCount()
            );
            postCache.put(id, updatedPost);
            log.info("Лайки увеличены для поста id={}, всего лайков={}", id, updatedLikes);
            return updatedLikes;
        } else {
            log.warn("Пост не найден при увеличении лайков: id={}", id);
            throw new IllegalStateException("Post not found with id " + id);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementCommentsCount(Long id) {
        log.debug("Инкремент комментариев для поста id={}", id);
        try {
            postRepository.incrementCommentsCount(id);
            PostResponse post = postCache.get(id);
            if (post != null) {
                int updatedCount = post.commentsCount() + 1;
                PostResponse updatedPost = new PostResponse(
                        post.id(),
                        post.title(),
                        post.text(),
                        post.tags(),
                        post.likesCount(),
                        updatedCount
                );
                postCache.put(id, updatedPost);
                log.info("Комментарии увеличены для поста id={}, всего комментариев={}", id, updatedCount);
            } else {
                log.warn("Пост не найден при увеличении комментариев: id={}", id);
            }
        } catch (Exception e) {
            log.error("Ошибка при увеличении комментариев для id={}: {}", id, e.getMessage(), e);
            throw new IllegalStateException("Failed to increment comments count for post id " + id, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrementCommentsCount(Long id) {
        log.debug("Декремент комментариев для поста id={}", id);
        try {
            postRepository.decrementCommentsCount(id);
            PostResponse post = postCache.get(id);
            if (post != null) {
                int updatedCount = Math.max(0, post.commentsCount() - 1);
                PostResponse updatedPost = new PostResponse(
                        post.id(),
                        post.title(),
                        post.text(),
                        post.tags(),
                        post.likesCount(),
                        updatedCount
                );
                postCache.put(id, updatedPost);
                log.info("Комментарии уменьшены для поста id={}, теперь={}", id, updatedCount);
            } else {
                log.warn("Пост не найден при уменьшении комментариев: id={}", id);
            }
        } catch (Exception e) {
            log.error("Ошибка при уменьшении комментариев для id={}: {}", id, e.getMessage(), e);
            throw new IllegalStateException("Failed to decrement comments count for post id " + id, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean postExists(Long postId) {
        return postCache.containsKey(postId);
    }
}
