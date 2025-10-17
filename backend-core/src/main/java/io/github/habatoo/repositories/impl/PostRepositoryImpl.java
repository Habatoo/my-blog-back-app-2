package io.github.habatoo.repositories.impl;

import io.github.habatoo.dto.request.PostCreateRequestDto;
import io.github.habatoo.dto.request.PostRequestDto;
import io.github.habatoo.dto.response.PostResponseDto;
import io.github.habatoo.repositories.PostRepository;
import io.github.habatoo.repositories.mapper.PostListRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static io.github.habatoo.repositories.sql.PostSqlQueries.*;

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
    public List<PostResponseDto> findAllPosts() {
        List<PostResponseDto> posts = jdbcTemplate.query(FIND_ALL_POSTS, postListRowMapper);

        return posts.stream()
                .map(this::enrichWithTags)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostResponseDto createPost(PostCreateRequestDto postCreateRequest) {
        LocalDateTime now = LocalDateTime.now();
        PostResponseDto postResponse = createPost(
                postCreateRequest.title(),
                postCreateRequest.text(),
                now);
        Long postId = postResponse.id();
        log.info("Пост успешно создан с id='{}'", postId);

        List<String> tags = postCreateRequest.tags();
        updatePostTagsInternal(postId, tags);
        List<String> tagsForPost = getTagsForPost(postId);

        return new PostResponseDto(
                postResponse.id(),
                postResponse.title(),
                postResponse.text(),
                tagsForPost,
                postResponse.likesCount(),
                postResponse.commentsCount()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostResponseDto updatePost(PostRequestDto postRequest) {
        Long postId = postRequest.id();
        PostResponseDto postResponse = updatePost(postRequest.title(),
                postRequest.text(),
                LocalDateTime.now(),
                postId);
        List<String> tags = postRequest.tags();
        updatePostTagsInternal(postId, tags);
        log.info("Пост id={} успешно обновлен", postId);

        List<String> tagsForPost = getTagsForPost(postId);

        return new PostResponseDto(
                postResponse.id(),
                postResponse.title(),
                postResponse.text(),
                tagsForPost,
                postResponse.likesCount(),
                postResponse.commentsCount()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePost(Long postId) {
        int deletedRows = jdbcTemplate.update(DELETE_POST, postId);
        String msg = String.format("Пост не найден для удаления id==%d", postId);
        checkIfThrow(deletedRows, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementLikes(Long postId) {
        int updatedRows = jdbcTemplate.update(INCREMENT_LIKES, postId);
        String msg = String.format("Пост не найден при увеличении лайков id=%d", postId);
        checkIfThrow(updatedRows, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementCommentsCount(Long postId) {
        int updatedRows = jdbcTemplate.update(INCREMENT_COMMENTS_COUNT, postId);
        String msg = String.format("Пост не найден при увеличении лайков id=%d", postId);
        checkIfThrow(updatedRows, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrementCommentsCount(Long postId) {
        int updatedRows = jdbcTemplate.update(DECREMENT_COMMENTS_COUNT, postId);
        String msg = String.format("Пост не найден при уменьшении лайков id=%d", postId);
        checkIfThrow(updatedRows, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getTagsForPost(Long postId) {
        try {
            return jdbcTemplate.queryForList(GET_TAGS_FOR_POST, String.class, postId);
        } catch (Exception e) {
            final String msg = String.format("Ошибка при получении тегов для поста id=%d", postId);
            log.warn(msg, e);
            return List.of();
        }
    }

    /**
     * Обогащает ответ.
     */
    private PostResponseDto enrichWithTags(PostResponseDto post) {
        List<String> tags = getTagsForPost(post.id());
        return new PostResponseDto(
                post.id(),
                post.title(),
                post.text(),
                tags,
                post.likesCount(),
                post.commentsCount()
        );
    }

    /**
     * Вставляет новый пост в таблицу и возвращает сгенерированный id.
     */
    private PostResponseDto createPost(
            String title,
            String text,
            LocalDateTime now) {
        return jdbcTemplate.queryForObject(
                CREATE_POST,
                postListRowMapper,
                title,
                text,
                Timestamp.valueOf(now),
                Timestamp.valueOf(now)
        );
    }

    /**
     * Редактирует существующий пост новый пост в таблице.
     */
    private PostResponseDto updatePost(
            String title,
            String text,
            LocalDateTime now,
            Long postId) {
        return jdbcTemplate.queryForObject(
                UPDATE_POST,
                postListRowMapper,
                title,
                text,
                Timestamp.valueOf(now),
                postId
        );
    }

    /**
     * Общий метод для создания / обновления тегов поста.
     */
    private void updatePostTagsInternal(Long postId, List<String> tags) {
        if (tags != null && !tags.isEmpty()) {
            deleteTags(postId);
            insertTags(tags);
            insertPostTags(postId, tags);
        }
    }

    /**
     * Выполняет удаление тэгов.
     */
    private void deleteTags(Long postId) {
        jdbcTemplate.update(DELETE_POST_TAGS, postId);
    }

    /**
     * Выполняет пакетную вставку тегов с использованием MERGE.
     */
    private void insertTags(List<String> tags) {
        jdbcTemplate.batchUpdate(
                INSERT_INTO_TAG,
                tags,
                tags.size(),
                (ps, tag) -> ps.setString(1, tag)
        );
    }

    /**
     * Выполняет пакетную вставку связей посты-теги с использованием MERGE.
     */
    private void insertPostTags(Long postId, List<String> tags) {
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

    /**
     * Проверяет ответ после обновления на не нулевое изменение в БД.
     */
    private void checkIfThrow(int parameter, String msg) {
        if (parameter == 0) {
            log.warn(msg);
            throw new IllegalStateException(msg);
        }
    }
}
