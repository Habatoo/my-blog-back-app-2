package io.github.habatoo.repositories.impl;

import io.github.habatoo.dto.request.CommentCreateRequestDto;
import io.github.habatoo.dto.response.CommentResponseDto;
import io.github.habatoo.repositories.CommentRepository;
import io.github.habatoo.repositories.mapper.CommentRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Реализация репозитория для работы с комментариями блога.
 * Обеспечивает доступ к данным комментариев с использованием JDBC Template.
 *
 * @see CommentRepository
 * @see JdbcTemplate
 * @see CommentRowMapper
 */
@Slf4j
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
    public List<CommentResponseDto> findByPostId(Long postId) {
        return jdbcTemplate.query(
                """
                        SELECT id, text, post_id
                        FROM comment
                        WHERE post_id = ?
                        ORDER BY created_at ASC
                        """,
                commentRowMapper,
                postId
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CommentResponseDto> findByPostIdAndId(Long postId, Long commentId) {
        List<CommentResponseDto> comments = jdbcTemplate.query(
                """
                        SELECT id, text, post_id
                        FROM comment
                        WHERE post_id = ? AND id = ?
                        """,
                commentRowMapper,
                postId,
                commentId
        );

        return comments.stream().findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentResponseDto save(CommentCreateRequestDto commentCreateRequest) {
        LocalDateTime now = LocalDateTime.now();

        return jdbcTemplate.queryForObject(
                """
                        INSERT INTO comment (post_id, text, created_at, updated_at)
                        VALUES (?, ?, ?, ?)
                        RETURNING id, text, post_id
                        """,
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
    public CommentResponseDto update(Long postId, Long commentId, String text) {
        return jdbcTemplate.queryForObject(
                """
                        UPDATE comment
                        SET text = ?, updated_at = ?
                        WHERE id = ?
                        RETURNING id, text, post_id
                        """,
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
        return jdbcTemplate.update(
                """
                        DELETE FROM comment WHERE id = ?
                        """,
                commentId
        );
    }
}
