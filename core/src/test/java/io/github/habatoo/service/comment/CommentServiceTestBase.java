package io.github.habatoo.service.comment;

import io.github.habatoo.dto.request.CommentCreateRequestDto;
import io.github.habatoo.dto.request.CommentRequestDto;
import io.github.habatoo.dto.response.CommentResponseDto;
import io.github.habatoo.repositories.CommentRepository;
import io.github.habatoo.service.CommentService;
import io.github.habatoo.service.PostService;
import io.github.habatoo.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Базовый класс для тестирования CommentServiceImpl
 */
@ExtendWith(MockitoExtension.class)
abstract class CommentServiceTestBase {

    @Mock
    protected CommentRepository commentRepository;

    @Mock
    protected PostService postService;

    protected CommentService commentService;

    protected static final Long VALID_POST_ID = 1L;
    protected static final Long VALID_COMMENT_ID = 2L;
    protected static final Long NON_EXISTENT_COMMENT_ID = 888L;
    protected static final String COMMENT_TEXT = "Тестовый комментарий";
    protected static final String UPDATED_COMMENT_TEXT = "Обновленный комментарий";

    @BeforeEach
    void setUp() {
        commentService = new CommentServiceImpl(commentRepository, postService);
    }

    protected CommentResponseDto createCommentResponse(Long commentId, Long postId, String text) {
        return new CommentResponseDto(commentId, text, postId);
    }

    protected CommentCreateRequestDto createCommentCreateRequest(String text, Long postId) {
        return new CommentCreateRequestDto(postId, text);
    }

    protected CommentRequestDto createUpdatedCommentRequestDto() {
        return new CommentRequestDto(VALID_COMMENT_ID, UPDATED_COMMENT_TEXT, VALID_POST_ID);
    }
}
