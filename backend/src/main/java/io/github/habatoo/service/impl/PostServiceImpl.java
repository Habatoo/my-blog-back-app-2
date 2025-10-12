package io.github.habatoo.service.impl;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostListResponse;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.repository.PostRepository;
import io.github.habatoo.service.FileStorageService;
import io.github.habatoo.service.PostService;
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
        List<PostResponse> allPosts = postRepository.findAllPosts();
        postCache.clear();
        allPosts.forEach(post -> postCache.put(post.id(), post));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostListResponse getPosts(String search, int pageNumber, int pageSize) {
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

        return new PostListResponse(page, fromIndex > 0, toIndex < totalCount, totalCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PostResponse> getPostById(Long id) {
        return Optional.ofNullable(postCache.get(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostResponse createPost(PostCreateRequest postCreateRequest) {
        try {
            PostResponse createdPost = postRepository.createPost(postCreateRequest);
            postCache.put(createdPost.id(), createdPost);
            return createdPost;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create post", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostResponse updatePost(PostRequest postRequest) { // TODO Ошибка: Failed to execute 'append' on 'FormData': parameter 2 is not of type 'Blob'.
        try {
            PostResponse updatedPost = postRepository.updatePost(postRequest);
            postCache.put(updatedPost.id(), updatedPost);
            return updatedPost;
        } catch (Exception e) {
            throw new IllegalStateException("Post not found or concurrently modified with id " + postRequest.id(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePost(Long id) {
        postRepository.deletePost(id);
        postCache.remove(id);
        fileStorageService.deletePostDirectory(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int incrementLikes(Long id) {
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
            return updatedLikes;
        } else {
            throw new IllegalStateException("Post not found with id " + id);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementCommentsCount(Long id) {
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
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to increment comments count for post id " + id, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrementCommentsCount(Long id) {
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
            }
        } catch (Exception e) {
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
