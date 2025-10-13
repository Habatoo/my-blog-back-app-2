package io.github.habatoo.repositories;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.repositories.impl.PostRepositoryImpl;
import org.springframework.data.repository.Repository;

import java.util.List;

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

    List<PostResponse> findAllPosts();


    PostResponse createPost(PostCreateRequest postCreateRequest);

    PostResponse updatePost(PostRequest postRequest);

    void deletePost(Long id);

    List<String> getTagsForPost(Long postId);

    void incrementLikes(Long postId);

    void incrementCommentsCount(Long postId);

    void decrementCommentsCount(Long postId);

}
