package io.github.habatoo.repositories.comment;

import io.github.habatoo.dto.request.CommentCreateRequestDto;
import io.github.habatoo.dto.response.CommentResponseDto;
import io.github.habatoo.repositories.CommentRepository;
import io.github.habatoo.repositories.impl.CommentRepositoryImpl;
import io.github.habatoo.repositories.mapper.CommentRowMapper;
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
    protected final String COMMENT_NEW_TEXT = "New test comment";

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