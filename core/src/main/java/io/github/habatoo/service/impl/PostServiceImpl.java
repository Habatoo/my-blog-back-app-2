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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    public PostServiceImpl(
            PostRepository postRepository,
            FileStorageService fileStorageService
    ) {
        this.postRepository = postRepository;
        this.fileStorageService = fileStorageService;
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

        List<PostResponseDto> page = postRepository.findPosts(searchPart, tags, pageNumber, pageSize);

        int totalCount = postRepository.countPosts(searchPart, tags);
        int lastPage = (int) Math.ceil((double) totalCount / pageSize);
        boolean hasPrev = pageNumber > 1;
        boolean hasNext = pageNumber < lastPage;

        log.debug("Всего найдено {} постов, lastPage: {}", totalCount, lastPage);

        return new PostListResponseDto(page, hasPrev, hasNext, lastPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PostResponseDto> getPostById(Long id) {
        log.debug("Получение поста по id={}", id);
        return postRepository.getPostById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostResponseDto createPost(PostCreateRequestDto postCreateRequest) {
        log.info("Создание нового поста: title='{}'", postCreateRequest.title());

        try {
            return postRepository.createPost(postCreateRequest);
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

        fileStorageService.deletePostDirectory(id);
        log.info("Пост и директория файлов удалены: id={}", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int incrementLikes(Long id) {
        log.debug("Инкремент лайков для поста id={}", id);
        try {
            postRepository.incrementLikes(id);
            Optional<PostResponseDto> post = postRepository.getPostById(id);

            if (post.isEmpty()) {
                log.warn("Пост после инкремента лайков не найден: id={}", id);
                throw new IllegalStateException("Пост не найден после увеличения лайков, id=" + id);
            }

            return post.get().likesCount();
        } catch (Exception e) {
            log.error("Ошибка при увеличении лайков для id={}: {}", id, e.getMessage(), e);
            throw new IllegalStateException("Ошибка при увеличении лайков для поста id " + id, e);
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
        } catch (Exception e) {
            log.error("Ошибка при уменьшении комментариев для id={}: {}", id, e.getMessage(), e);
            throw new IllegalStateException("Ошибка при уменьшении комментариев для поста id " + id, e);
        }
    }
}
