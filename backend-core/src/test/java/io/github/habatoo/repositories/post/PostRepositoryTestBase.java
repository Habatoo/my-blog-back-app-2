package io.github.habatoo.repositories.post;

import io.github.habatoo.dto.request.PostCreateRequestDto;
import io.github.habatoo.dto.response.PostResponseDto;
import io.github.habatoo.repositories.impl.PostRepositoryImpl;
import io.github.habatoo.repositories.mapper.PostListRowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Stream;

/**
 * Базовый класс для тестирования CommentRepositoryImpl.
 */
@ExtendWith(MockitoExtension.class)
abstract class PostRepositoryTestBase {

    @Mock
    protected JdbcTemplate jdbcTemplate;

    @Mock
    protected PostListRowMapper postListRowMapper;

    @InjectMocks
    protected PostRepositoryImpl postRepository;

    protected static final Long POST_ID = 1L;
    protected static final Long NON_EXISTING_POST_ID = 999L;
    protected static final String TITLE = "Test title";
    protected static final String TEXT = "Test text";
    protected static final List<String> TAGS = List.of("tag1", "tag2");

    protected PostResponseDto createPostDto(Long id, List<String> tags) {
        return new PostResponseDto(id, TITLE, TEXT, tags, 0, 0);
    }

    @BeforeEach
    void setUp() {
        postRepository = new PostRepositoryImpl(jdbcTemplate, postListRowMapper);
    }

    protected static Stream<Arguments> posts() {
        return Stream.of(
                Arguments.of(
                        new PostCreateRequestDto("title1", "text1", List.of("t1", "t2")),
                        new PostResponseDto(POST_ID, "title1", "text1", List.of("t1", "t2"), 0, 0),
                        true
                ),
                Arguments.of(
                        new PostCreateRequestDto("title2", "text2", List.of()),
                        new PostResponseDto(POST_ID, "title2", "text2", List.of(), 0, 0),
                        false
                )
        );
    }
}
