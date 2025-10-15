package io.github.habatoo.service.impl;

import io.github.habatoo.dto.request.PostCreateRequestDto;
import io.github.habatoo.dto.request.PostRequestDto;
import io.github.habatoo.dto.response.PostListResponseDto;
import io.github.habatoo.dto.response.PostResponseDto;
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
 * @see PostResponseDto
 * @see FileStorageService
 */
@Slf4j
@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final FileStorageService fileStorageService;

    private final Map<Long, PostResponseDto> postCache = new ConcurrentHashMap<>();

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
        List<PostResponseDto> allPosts = postRepository.findAllPosts();
        postCache.clear();
        allPosts.forEach(post -> postCache.put(post.id(), post));
        log.info("Кеш загружен, постов: {}", allPosts.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostListResponseDto getPosts(String search, int pageNumber, int pageSize) {
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

        List<PostResponseDto> filtered = postCache.values().stream()
                .filter(post -> searchPart.isEmpty()
                        || post.title().contains(searchPart)
                        || post.text().contains(searchPart))
                .filter(post -> tags.isEmpty()
                        || tags.stream().allMatch(tag ->
                        post.tags().stream().anyMatch(t -> t.equals(tag))
                ))
                .sorted(Comparator.comparing(PostResponseDto::id))
                .toList();

        int totalCount = filtered.size();
        int fromIndex = Math.min((pageNumber - 1) * pageSize, totalCount);
        int toIndex = Math.min(fromIndex + pageSize, totalCount);
        List<PostResponseDto> page = filtered.subList(fromIndex, toIndex);

        log.debug("Фильтровано {} постов, от {} до {}", totalCount, fromIndex, toIndex);

        return new PostListResponseDto(page, fromIndex > 0, toIndex < totalCount, totalCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PostResponseDto> getPostById(Long id) {
        log.debug("Получение поста по id={}", id);

        return Optional.ofNullable(postCache.get(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostResponseDto createPost(PostCreateRequestDto postCreateRequest) {
        log.info("Создание нового поста: title='{}'", postCreateRequest.title());

        try {
            PostResponseDto createdPost = postRepository.createPost(postCreateRequest);
            postCache.put(createdPost.id(), createdPost);

            return createdPost;
        } catch (Exception e) {
            log.error("Не удалось создать пост: {}", e.getMessage(), e);
            throw new IllegalStateException("Не удалось создать пост", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostResponseDto updatePost(PostRequestDto postRequest) {
        log.info("Обновление поста: id={}", postRequest.id());

        try {
            PostResponseDto updatedPost = postRepository.updatePost(postRequest);
            postCache.put(updatedPost.id(), updatedPost);
            log.info("Пост обновлён: id={}", updatedPost.id());
            return updatedPost;
        } catch (Exception e) {
            log.error("Ошибка при обновлении поста id={}: {}", postRequest.id(), e.getMessage(), e);
            throw new IllegalStateException("Пост не найден или уже изменен с id " + postRequest.id(), e);
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
        PostResponseDto post = postCache.get(id);
        if (post != null) {
            int updatedLikes = post.likesCount() + 1;
            PostResponseDto updatedPost = new PostResponseDto(
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
            throw new IllegalStateException("Пост не найден with id " + id);
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
            PostResponseDto post = postCache.get(id);
            if (post != null) {
                int updatedCount = post.commentsCount() + 1;
                PostResponseDto updatedPost = new PostResponseDto(
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
            throw new IllegalStateException("Ошибка при увеличении комментариев для поста id " + id, e);
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
            PostResponseDto post = postCache.get(id);
            if (post != null) {
                int updatedCount = Math.max(0, post.commentsCount() - 1);
                PostResponseDto updatedPost = new PostResponseDto(
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
            throw new IllegalStateException("Ошибка при уменьшении комментариев для поста id " + id, e);
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
