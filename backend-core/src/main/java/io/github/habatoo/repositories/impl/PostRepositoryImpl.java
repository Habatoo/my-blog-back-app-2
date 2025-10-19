package io.github.habatoo.repositories.impl;

import io.github.habatoo.dto.request.PostCreateRequestDto;
import io.github.habatoo.dto.request.PostRequestDto;
import io.github.habatoo.dto.response.PostResponseDto;
import io.github.habatoo.repositories.PostRepository;
import io.github.habatoo.repositories.mapper.PostListRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

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
    public List<PostResponseDto> findPosts(String searchPart, List<String> tags, int pageNumber, int pageSize) {
        Map<String, List<String>> sbResult = buildWhereClause(searchPart, tags);
        String where = sbResult.keySet().iterator().next();
        List<Object> params = new ArrayList<>(sbResult.get(where));

        String sql = """
                SELECT p.id, p.title, p.text, p.likes_count, p.comments_count
                FROM post p
                """ + where + " ORDER BY p.created_at DESC LIMIT ? OFFSET ?";
        params.add(pageSize);
        params.add((pageNumber - 1) * pageSize);

        List<PostResponseDto> posts = jdbcTemplate.query(
                sql,
                params.toArray(),
                postListRowMapper
        );
        return posts.stream()
                .map(this::enrichWithTags)
                .toList();
    }

    @Override
    public int countPosts(String searchPart, List<String> tags) {
        Map<String, List<String>> sbResult = buildWhereClause(searchPart, tags);
        String where = sbResult.keySet().iterator().next();
        List<String> params = sbResult.get(where);

        String sql = "SELECT COUNT(*) FROM post p" + where;
        Integer count = jdbcTemplate.queryForObject(
                sql,
                params.toArray(),
                Integer.class
        );
        return count == null ? 0 : count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PostResponseDto> getPostById(Long postId) {
        try {
            PostResponseDto post = jdbcTemplate.queryForObject(
                    """
                                SELECT id, title, text, likes_count, comments_count
                                FROM post
                                WHERE id = ?
                            """,
                    postListRowMapper,
                    postId
            );

            return Optional.ofNullable(post)
                    .map(this::enrichWithTags);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Пост с id={} не найден", postId);
            return Optional.empty();
        }
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
        int deletedRows = jdbcTemplate.update(
                """
                        DELETE FROM post WHERE id = ?
                        """,
                postId
        );
        String msg = String.format("Пост не найден для удаления id==%d", postId);
        checkIfThrow(deletedRows, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementLikes(Long postId) {
        int updatedRows = jdbcTemplate.update(
                """
                        UPDATE post SET likes_count = likes_count + 1 WHERE id = ?
                        """,
                postId
        );
        String msg = String.format("Пост не найден при увеличении лайков id=%d", postId);
        checkIfThrow(updatedRows, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementCommentsCount(Long postId) {
        int updatedRows = jdbcTemplate.update(
                """
                        UPDATE post SET comments_count = comments_count + 1 WHERE id = ?
                        """,
                postId
        );
        String msg = String.format("Пост не найден при увеличении лайков id=%d", postId);
        checkIfThrow(updatedRows, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrementCommentsCount(Long postId) {
        int updatedRows = jdbcTemplate.update(
                """
                        UPDATE post
                        SET comments_count = CASE WHEN comments_count > 0 THEN comments_count - 1 ELSE 0 END
                        WHERE id = ?
                        """,
                postId
        );
        String msg = String.format("Пост не найден при уменьшении лайков id=%d", postId);
        checkIfThrow(updatedRows, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getTagsForPost(Long postId) {
        try {
            return jdbcTemplate.queryForList(
                    """
                            SELECT t.name FROM tag t
                            JOIN post_tag pt ON t.id = pt.tag_id
                            WHERE pt.post_id = ?
                            """,
                    String.class,
                    postId
            );
        } catch (Exception e) {
            final String msg = String.format("Ошибка при получении тегов для поста id=%d", postId);
            log.warn(msg, e);
            return List.of();
        }
    }

    /**
     * Постороение условий поиска.
     */
    private Map<String, List<String>> buildWhereClause(String searchPart, List<String> tags) {
        List<String> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();
        if (!searchPart.isBlank()) {
            conditions.add("(p.title LIKE ? OR p.text LIKE ?)");
            params.add("%" + searchPart + "%");
            params.add("%" + searchPart + "%");
        }
        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                conditions.add("""
                            EXISTS (
                                SELECT 1
                                FROM post_tag pt
                                JOIN tag t ON t.id = pt.tag_id
                                WHERE pt.post_id = p.id AND t.name = ?
                            )
                        """);
                params.add(tag);
            }
        }
        String whereClause = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
        Map<String, List<String>> result = new HashMap<>();
        result.put(whereClause, params);

        return result;
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
                """
                        INSERT INTO post (title, text, likes_count, comments_count, created_at, updated_at)
                        VALUES (?, ?, 0, 0, ?, ?)
                        RETURNING id, title, text, likes_count, comments_count
                        """,
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
                """
                        UPDATE post
                        SET title = ?, text = ?, updated_at = ?
                        WHERE id = ?
                        RETURNING id, title, text, likes_count, comments_count
                        """,
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
        jdbcTemplate.update(
                """
                        DELETE FROM post_tag WHERE post_id = ?
                        """,
                postId
        );
    }

    /**
     * Выполняет пакетную вставку тегов с использованием batchUpdate.
     */
    private void insertTags(List<String> tags) {
        jdbcTemplate.batchUpdate(
                """
                        INSERT INTO tag (name)
                        VALUES (?)
                        ON CONFLICT (name) DO NOTHING;
                        """,
                tags,
                tags.size(),
                (ps, tag) -> ps.setString(1, tag)
        );
    }

    /**
     * Выполняет пакетную вставку связей посты-теги с использованием batchUpdate.
     */
    private void insertPostTags(Long postId, List<String> tags) {
        jdbcTemplate.batchUpdate(
                """
                        INSERT INTO post_tag (post_id, tag_id)
                        VALUES (?, (SELECT id FROM tag WHERE name = ?))
                        ON CONFLICT (post_id, tag_id) DO NOTHING;
                        """,
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
