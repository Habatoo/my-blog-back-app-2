package io.github.habatoo.repositories.impl;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.repositories.PostRepository;
import io.github.habatoo.repositories.mapper.PostListRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
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
    public List<PostResponse> findAllPosts() {
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
        LocalDateTime now = LocalDateTime.now();
        Long postId = createPost(postCreateRequest.title(), postCreateRequest.text(), now);
        log.info("Пост успешно создан с id='{}'", postId);

        List<String> tags = postCreateRequest.tags();
        updatePostTagsInternal(postId, tags);

        return selectPostById(postId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostResponse updatePost(PostRequest postRequest) {
        Long postId = postRequest.id();
        updatePost(postRequest.title(),
                postRequest.text(),
                LocalDateTime.now(),
                postId);
        List<String> tags = postRequest.tags();
        updatePostTagsInternal(postId, tags);
        log.info("Пост id={} успешно обновлен", postId);

        return selectPostById(postId);
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

    /**
     * Вставляет новый пост в таблицу и возвращает сгенерированный id.
     */
    private Long createPost(String title, String text, LocalDateTime now) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    CREATE_POST,
                    new String[]{"ID"}
            );
            ps.setString(1, title);
            ps.setString(2, text);
            ps.setTimestamp(3, Timestamp.valueOf(now));
            ps.setTimestamp(4, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() == null) {
            String msg = "Пост не создан";
            log.warn(msg);
            throw new IllegalStateException(msg);
        }

        return keyHolder.getKey().longValue();
    }

    /**
     * Редактирует существующий пост новый пост в таблице.
     */
    private void updatePost(String title,
                            String text,
                            LocalDateTime now,
                            Long postId) {
        int rowsUpdated = jdbcTemplate.update(
                UPDATE_POST,
                title,
                text,
                now,
                postId);
        final String msg = String.format("Пост с id=%d не найден для обновления", postId);
        checkIfThrow(rowsUpdated, msg);
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
     * Выбирает пост по id.
     */
    private PostResponse selectPostById(Long postId) {
        PostResponse post = jdbcTemplate.queryForObject(
                SELECT_POST_BY_ID,
                postListRowMapper,
                postId
        );
        return enrichWithTags(post);
    }

    /**
         * Проверяет ответ после обновления на не нулевое изменение в БД.
         */
        private void checkIfThrow ( int parameter, String msg){
            if (parameter == 0) {
                log.warn(msg);
                throw new IllegalStateException(msg);
            }
        }
    }
