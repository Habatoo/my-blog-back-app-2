package io.github.habatoo.repository.impl;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.repository.CommentRepository;
import io.github.habatoo.repository.mapper.CommentRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static io.github.habatoo.repository.sql.CommentSqlQueries.*;

/**
 * Реализация репозитория для работы с комментариями блога.
 * Обеспечивает доступ к данным комментариев с использованием JDBC Template.
 *
 * @see CommentRepository
 * @see JdbcTemplate
 * @see CommentRowMapper
 */
@Repository
public class CommentRepositoryImpl implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CommentRowMapper commentRowMapper;

    public CommentRepositoryImpl(
            JdbcTemplate jdbcTemplate,
            CommentRowMapper commentRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.commentRowMapper = commentRowMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CommentResponse> findByPostId(Long postId) {
        return jdbcTemplate.query(FIND_BY_POST_ID, commentRowMapper, postId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CommentResponse> findByPostIdAndId(Long postId, Long commentId) {
        List<CommentResponse> comments = jdbcTemplate.query(FIND_BY_POST_ID_AND_ID, commentRowMapper, postId, commentId);
        return comments.stream().findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentResponse save(CommentCreateRequest commentCreateRequest) {
        LocalDateTime now = LocalDateTime.now();

        return jdbcTemplate.queryForObject(
                INSERT_COMMENT,
                commentRowMapper,
                commentCreateRequest.postId(),
                commentCreateRequest.text(),
                Timestamp.valueOf(now),
                Timestamp.valueOf(now)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentResponse updateText(Long commentId, String text) {
        return jdbcTemplate.queryForObject(
                UPDATE_COMMENT_TEXT,
                commentRowMapper,
                text,
                Timestamp.valueOf(LocalDateTime.now()),
                commentId
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int deleteById(Long commentId) {
        return jdbcTemplate.update(DELETE_COMMENT, commentId);
    }
}
