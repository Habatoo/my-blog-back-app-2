package io.github.habatoo.repositories.comment;

import io.github.habatoo.dto.request.CommentCreateRequestDto;
import io.github.habatoo.dto.response.CommentResponseDto;
import io.github.habatoo.repositories.CommentRepository;
import io.github.habatoo.repositories.impl.CommentRepositoryImpl;
import io.github.habatoo.repositories.mapper.CommentRowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

/**
 * Базовый класс для тестирования CommentRepositoryImpl.
 */
@ExtendWith(MockitoExtension.class)
abstract class CommentRepositoryTestBase {

    @Mock
    protected JdbcTemplate jdbcTemplate;

    @Mock
    protected CommentRowMapper commentRowMapper;

    @InjectMocks
    protected CommentRepositoryImpl commentRepository;

    protected static final Long COMMENT_ID = 1L;
    protected static final Long POST_ID = 10L;
    protected static final String COMMENT_TEXT = "Some comment text";
    protected static final String UPDATED_TEXT = "Updated comment text";

    @BeforeEach
    void setUp() {
        commentRepository = new CommentRepositoryImpl(jdbcTemplate, commentRowMapper);
    }

    protected CommentResponseDto createCommentResponse(Long id, Long postId, String text) {
        return new CommentResponseDto(id, text, postId);
    }

    protected CommentCreateRequestDto createCommentCreateRequest(String text, Long postId) {
        return new CommentCreateRequestDto(postId, text);
    }
}