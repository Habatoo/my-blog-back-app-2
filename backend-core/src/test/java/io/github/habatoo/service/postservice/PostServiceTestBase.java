package io.github.habatoo.service.postservice;

import io.github.habatoo.dto.response.PostResponseDto;
import io.github.habatoo.repositories.PostRepository;
import io.github.habatoo.service.FileStorageService;
import io.github.habatoo.service.PostService;
import io.github.habatoo.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Базовый класс для тестирования PostServiceImpl
 */
@ExtendWith(MockitoExtension.class)
abstract class PostServiceTestBase {

    @Mock
    protected PostRepository postRepository;

    @Mock
    protected FileStorageService fileStorageService;

    protected PostService postService;

    protected static final Long VALID_POST_ID = 1L;
    protected static final Long INVALID_POST_ID = 999L;

    protected static final PostResponseDto POST_RESPONSE_1 = new PostResponseDto(1L, "Первый", "Текст 1", List.of("tag1", "tag2"), 5, 10);
    protected static final PostResponseDto POST_RESPONSE_1_LIKES = new PostResponseDto(1L, "Первый", "Текст 1", List.of("tag1", "tag2"), 6, 10);

    @BeforeEach
    void setUp() {
        postService = new PostServiceImpl(postRepository, fileStorageService);
    }

    static Stream<Arguments> postIdProvider() {
        return Stream.of(
                Arguments.of(VALID_POST_ID, true),
                Arguments.of(INVALID_POST_ID, false)
        );
    }

    protected static Stream<Arguments> provideSearchFilters() {
        List<PostResponseDto> allPosts = List.of(
                new PostResponseDto(1L, "Spring Framework", "Spring — это каркас...", List.of("java", "backend"), 5, 2),
                new PostResponseDto(2L, "PostgreSQL Integration", "Настроим postgres...", List.of("db", "backend"), 3, 1),
                new PostResponseDto(3L, "Советы по Markdown", "Учимся оформлять post", List.of("markdown"), 1, 0),
                new PostResponseDto(4L, "Framework Tips", "Framework rocks!", List.of("framework"), 2, 5),
                new PostResponseDto(5L, "Java Collections", "about HashMap и List", List.of("java"), 4, 3)
        );
        return Stream.of(
                Arguments.of("", 5, allPosts),
                Arguments.of("Spring", 1, allPosts),
                Arguments.of("Framework", 2, allPosts),
                Arguments.of("#java", 2, allPosts),
                Arguments.of("Spring #java", 1, allPosts),
                Arguments.of("#backend", 2, allPosts),
                Arguments.of("#unknowntag", 0, allPosts),
                Arguments.of("Hash", 1, allPosts),
                Arguments.of("Java", 1, allPosts),
                Arguments.of("Markdown", 1, allPosts)
        );
    }

    protected static Stream<Arguments> providePagination() {
        List<PostResponseDto> posts = IntStream.rangeClosed(1, 53)
                .mapToObj(i -> new PostResponseDto(
                        (long) i,
                        "Title " + i,
                        "Text " + i,
                        List.of("tag" + (i % 5)),
                        i, i
                ))
                .toList();
        return Stream.of(
                Arguments.of(1, 1, 1, posts),
                Arguments.of(5, 1, 5, posts),
                Arguments.of(10, 2, 10, posts),
                Arguments.of(20, 3, 13, posts),
                Arguments.of(50, 2, 3, posts)
        );
    }
}
