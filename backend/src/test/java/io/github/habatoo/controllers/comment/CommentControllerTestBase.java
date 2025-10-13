package io.github.habatoo.controllers.comment;

import io.github.habatoo.controllers.CommentController;
import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.request.CommentRequest;
import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

/**
 * Настройки тестов на покрытие основных сценариев работы контроллера,
 * включая успешные операции,
 * обработку отсутствующих данных и граничные значения параметров.
 */
@ExtendWith(MockitoExtension.class)
public abstract class CommentControllerTestBase {

    protected static final Long VALID_POST_ID = 1L;
    protected static final Long VALID_COMMENT_ID = 2L;
    protected static final Long NON_EXISTENT_COMMENT_ID = 888L;
    protected static final String COMMENT_TEXT = "Тестовый комментарий";
    protected static final String UPDATED_COMMENT_TEXT = "Обновленный комментарий";

    @Mock
    protected CommentService commentService;

    protected CommentController commentController;

    @BeforeEach
    void setUp() {
        commentController = new CommentController(commentService);
    }

    protected CommentResponse createCommentResponse(Long id, Long postId, String text) {
        return new CommentResponse(id, text, postId);
    }

    protected CommentCreateRequest createCommentCreateRequest(String text, Long postId) {
        return new CommentCreateRequest(postId, text);
    }

    protected CommentRequest createCommentRequest(Long id, String text, Long postId) {
        return new CommentRequest(id, text, postId);
    }

    protected List<CommentResponse> createCommentList(Long postId) {
        return List.of(
                createCommentResponse(1L, postId, "Первый комментарий"),
                createCommentResponse(2L, postId, "Второй комментарий"),
                createCommentResponse(3L, postId, "Третий комментарий")
        );
    }
}
