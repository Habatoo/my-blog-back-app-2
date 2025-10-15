package io.github.habatoo.controllers.post;

import io.github.habatoo.controllers.PostController;
import io.github.habatoo.dto.request.PostCreateRequestDto;
import io.github.habatoo.dto.request.PostRequestDto;
import io.github.habatoo.dto.response.PostListResponseDto;
import io.github.habatoo.dto.response.PostResponseDto;
import io.github.habatoo.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

/**
 * Настройки тестов на покрытие основных сценариев работы контроллера,
 * включая успешные операции,
 * обработку граничных значений параметров и специфичные требования к форматам ответов.
 */
@ExtendWith(MockitoExtension.class)
public abstract class PostControllerTestBase {

    protected static final Long VALID_POST_ID = 1L;
    protected static final Long NON_EXISTENT_POST_ID = 999L;
    protected static final String SEARCH_QUERY = "test";
    protected static final int VALID_PAGE_NUMBER = 1;
    protected static final int VALID_PAGE_SIZE = 10;
    protected static final String POST_TITLE = "Тестовый пост";
    protected static final String POST_TEXT = "Текст поста в формате Markdown";
    protected static final List<String> POST_TAGS = List.of("tag1", "tag2");

    @Mock
    protected PostService postService;

    protected PostController postController;

    @BeforeEach
    void setUp() {
        postController = new PostController(postService);
    }

    protected PostResponseDto createPostResponse(Long id, String title, String text, List<String> tags,
                                                 int likesCount, int commentsCount) {
        return new PostResponseDto(id, title, text, tags, likesCount, commentsCount);
    }

    protected PostListResponseDto createPostListResponse(List<PostResponseDto> posts, boolean hasPrev,
                                                         boolean hasNext, int lastPage) {
        return new PostListResponseDto(posts, hasPrev, hasNext, lastPage);
    }

    protected PostCreateRequestDto createPostCreateRequest(String title, String text, List<String> tags) {
        return new PostCreateRequestDto(title, text, tags);
    }

    protected PostRequestDto createPostRequest(Long id, String title, String text, List<String> tags) {
        return new PostRequestDto(id, title, text, tags);
    }

    protected List<PostResponseDto> createPostList() {
        return List.of(
                createPostResponse(
                        1L,
                        "Пост 1",
                        "Текст 1",
                        List.of("tag1"),
                        5,
                        2),
                createPostResponse(
                        2L,
                        "Пост 2",
                        "Текст 2",
                        List.of("tag2"),
                        3,
                        1),
                createPostResponse(
                        3L,
                        "Пост 3",
                        "Текст 3",
                        List.of("tag1", "tag3"),
                        10,
                        5)
        );
    }

}
