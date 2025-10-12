package io.github.habatoo.repository.comment;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.repository.CommentRepository;
import io.github.habatoo.repository.impl.CommentRepositoryImpl;
import io.github.habatoo.repository.mapper.CommentRowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Базовый класс для тестирования CommentRepositoryImpl.
 */
@ExtendWith(MockitoExtension.class)
abstract class CommentRepositoryTestBase {

    @Mock
    protected JdbcTemplate jdbcTemplate;

    protected CommentRowMapper commentRowMapper = new CommentRowMapper();

    protected CommentRepository commentRepository;

    protected final Long POST_ID = 1L;
    protected final Long COMMENT_ID = 2L;
    protected final String COMMENT_TEXT = "Test comment";

    @BeforeEach
    void setUp() {
        commentRepository = new CommentRepositoryImpl(jdbcTemplate, commentRowMapper);
    }

    protected CommentResponse createCommentResponse(Long id, Long postId, String text) {
        return new CommentResponse(id, text, postId);
    }

    protected CommentCreateRequest createCommentCreateRequest(String text, Long postId) {
        return new CommentCreateRequest(text, postId);
    }
}