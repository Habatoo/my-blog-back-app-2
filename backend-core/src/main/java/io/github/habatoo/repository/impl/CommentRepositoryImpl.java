package io.github.habatoo.repository.impl;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.repository.CommentRepository;
import io.github.habatoo.repository.mapper.CommentRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
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
    public List<CommentResponse> findByPostId(Long postId) {
        log.debug("Выполняется поиск комментариев для поста id={}", postId);

        return jdbcTemplate.query(FIND_BY_POST_ID, commentRowMapper, postId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CommentResponse> findByPostIdAndId(Long postId, Long commentId) {
        log.debug("Выполняется поиск комментария id={} для поста id={}", commentId, postId);
        List<CommentResponse> comments = jdbcTemplate.query(FIND_BY_POST_ID_AND_ID, commentRowMapper, postId, commentId);

        return comments.stream().findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentResponse save(CommentCreateRequest commentCreateRequest) {
        log.info("Сохраняется новый комментарий к посту id={}", commentCreateRequest.postId());
        LocalDateTime now = LocalDateTime.now();

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    INSERT_COMMENT,
                    new String[]{"ID"}
            );
            ps.setLong(1, commentCreateRequest.postId());
            ps.setString(2, commentCreateRequest.text());
            ps.setTimestamp(3, Timestamp.valueOf(now));
            ps.setTimestamp(4, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        Long commentId = keyHolder.getKey().longValue();

        return findById(commentId).orElseThrow(() -> new IllegalStateException("Комментарий не сохранен"));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CommentResponse updateText(Long commentId, String text) {
        log.info("Обновляется текст комментария id={}", commentId);

        int updatedRows = jdbcTemplate.update(
                UPDATE_COMMENT_TEXT,
                text,
                Timestamp.valueOf(LocalDateTime.now()),
                commentId);
        if (updatedRows == 0) {
            throw new IllegalStateException("Комментарий с id=" + commentId + " не найден для обновления");
        }

        return jdbcTemplate.query(FIND_BY_ID, commentRowMapper, commentId).stream().findFirst().orElseThrow(() -> new IllegalStateException("Комментарий не обновлен"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int deleteById(Long commentId) {
        log.info("Удаляется комментарий id={}", commentId);

        return jdbcTemplate.update(DELETE_COMMENT, commentId);
    }

    private Optional<CommentResponse> findById(Long commentId) {
        log.debug("Выполняется поиск комментария id={}", commentId);

        return jdbcTemplate.query(FIND_BY_ID, commentRowMapper, commentId).stream().findFirst();
    }
}
