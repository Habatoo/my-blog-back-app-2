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

    protected static Stream<Arguments> provideParameters() {
        return Stream.of(
                Arguments.of("", List.of(), 1, 1),      // пустой searchPart, пустые теги, pageSize=1
                Arguments.of("java", List.of(), 1, 5),  // не пустой searchPart, пустые теги, pageSize=5
                Arguments.of("", List.of("spring"), 1, 10), // пустой поиск, один тег, pageSize=10
                Arguments.of("blog", List.of("java", "spring"), 2, 20), // всё заполнено, списочные теги, pageSize=20
                Arguments.of("", List.of("spring", "boot", "java"), 3, 50), // пустой поиск, много тегов, pageSize=50
                Arguments.of("запуск", List.of(), 1, 1),   // разные поисковые строки
                Arguments.of("отчет", List.of("boot"), 2, 5)
        );
    }

    static Stream<Arguments> countPostsParameters() {
        return Stream.of(
                Arguments.of("", List.of(), 15, 15),                 // пусто, пусто, в базе 15
                Arguments.of("Java", List.of(), 5, 5),               // поиск, пусто
                Arguments.of("", List.of("spring"), 3, 3),           // только тег
                Arguments.of("blog", List.of("java", "spring"), 2, 2), // поиск + мульти тег
                Arguments.of("none", List.of("none"), null, 0)       // нет результатов
        );
    }

    protected static Stream<Arguments> getPostByIdParameters() {
        return Stream.of(
                Arguments.of(1L, true),    // существует
                Arguments.of(42L, false),  // не существует
                Arguments.of(7L, true),
                Arguments.of(99L, false)
        );
    }
}
