package io.github.habatoo.service.postservice;

import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.repository.PostRepository;
import io.github.habatoo.service.FileStorageService;
import io.github.habatoo.service.PostService;
import io.github.habatoo.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;

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

    protected static final PostResponse POST_RESPONSE_1 = new PostResponse(1L, "Первый", "Текст 1", List.of("tag1", "tag2"), 5, 10);
    protected static final PostResponse POST_RESPONSE_2 = new PostResponse(2L, "Второй", "Текст 2", List.of("tag2", "tag3"), 3, 5);
    protected static final PostResponse POST_RESPONSE_3 = new PostResponse(3L, "Третий", "Текст 3", List.of(), 0, 0);

    @BeforeEach
    void setUp() {
        when(postRepository.findAllPosts()).thenReturn(List.of(POST_RESPONSE_1, POST_RESPONSE_2, POST_RESPONSE_3));
        postService = new PostServiceImpl(postRepository, fileStorageService);
    }

    static Stream<Arguments> postIdProvider() {
        return Stream.of(
                Arguments.of(VALID_POST_ID, true),
                Arguments.of(INVALID_POST_ID, false)
        );
    }


}
