package io.github.habatoo.repository.impl;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.repository.PostRepository;
import io.github.habatoo.repository.mapper.PostListRowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static io.github.habatoo.repository.sql.PostSqlQueries.*;

/**
 * Реализация репозитория для работы с постами блога.
 * Обеспечивает доступ к данным постов с использованием JDBC Template
 * (только CRUD операции).
 *
 * @see PostListRowMapper
 * @see JdbcTemplate
 */
@Slf4j
@Repository
public class PostRepositoryImpl implements PostRepository {

    private final JdbcTemplate jdbcTemplate;
    private final PostListRowMapper postListRowMapper;

    public PostRepositoryImpl(JdbcTemplate jdbcTemplate,
                              PostListRowMapper postListRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.postListRowMapper = postListRowMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PostResponse> findAllPosts() {
        log.debug("Получение всех постов");
        List<PostResponse> posts = jdbcTemplate.query(FIND_ALL_POSTS, postListRowMapper);

        return posts.stream()
                .map(this::enrichWithTags)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostResponse createPost(PostCreateRequest postCreateRequest) {
        log.info("Создание нового поста с title='{}'", postCreateRequest.title());
        LocalDateTime now = LocalDateTime.now();
        PostResponse post = jdbcTemplate.queryForObject(
                CREATE_POST,
                postListRowMapper,
                postCreateRequest.title(),
                postCreateRequest.text(),
                Timestamp.valueOf(now),
                Timestamp.valueOf(now)
        );

        long postId = post.id();
        List<String> tags = postCreateRequest.tags();
        if (!tags.isEmpty()) {
            log.debug("Добавление тегов {} к посту id={}", tags, postId);
            jdbcTemplate.batchUpdate(
                    INSERT_INTO_TAG,
                    tags,
                    tags.size(),
                    (ps, tag) -> ps.setString(1, tag)
            );

            jdbcTemplate.batchUpdate(
                    INSERT_INTO_POST_TAG,
                    tags,
                    tags.size(),
                    (ps, tag) -> {
                        ps.setLong(1, postId);
                        ps.setString(2, tag);
                    }
            );
        }

        return new PostResponse(
                postId, post.title(), post.text(), tags,
                post.likesCount(), post.commentsCount()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostResponse updatePost(PostRequest postRequest) {
        log.info("Обновление поста id={}", postRequest.id());

        return jdbcTemplate.queryForObject(
                UPDATE_POST,
                postListRowMapper,
                postRequest.title(),
                postRequest.text(),
                Timestamp.valueOf(LocalDateTime.now()),
                postRequest.id()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePost(Long id) {
        log.info("Удаление поста id={}", id);
        int deletedRows = jdbcTemplate.update(DELETE_POST, id);
        if (deletedRows == 0) {
            log.warn("Пост не найден для удаления id={}", id);
            throw new IllegalStateException("Post to delete not found with id " + id);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getTagsForPost(Long postId) {
        log.debug("Запрос тегов для поста id={}", postId);
        try {
            return jdbcTemplate.query(GET_TAGS_FOR_POST,
                    (rs, rowNum) -> rs.getString("name"),
                    postId
            );
        } catch (Exception e) {
            log.warn("Ошибка при получении тегов для поста id={}: {}", postId, e.getMessage());
            return List.of();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementLikes(Long postId) {
        log.info("Увеличение лайков для поста id={}", postId);
        int updatedRows = jdbcTemplate.update(INCREMENT_LIKES, postId);
        if (updatedRows == 0) {
            log.warn("Пост не найден при увеличении лайков id={}", postId);
            throw new EmptyResultDataAccessException("Post with id " + postId + " not found", 1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementCommentsCount(Long postId) {
        log.debug("Увеличение счётчика комментариев для поста id={}", postId);
        jdbcTemplate.update(INCREMENT_COMMENTS_COUNT, postId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrementCommentsCount(Long postId) {
        log.debug("Уменьшение счётчика комментариев для поста id={}", postId);
        jdbcTemplate.update(DECREMENT_COMMENTS_COUNT, postId);
    }

    private PostResponse enrichWithTags(PostResponse post) {
        List<String> tags = getTagsForPost(post.id());
        return new PostResponse(
                post.id(),
                post.title(),
                post.text(),
                tags,
                post.likesCount(),
                post.commentsCount()
        );
    }
}
