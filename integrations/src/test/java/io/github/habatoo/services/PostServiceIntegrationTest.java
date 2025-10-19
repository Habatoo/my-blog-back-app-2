package io.github.habatoo.services;

import io.github.habatoo.configurations.TestDataSourceConfiguration;
import io.github.habatoo.configurations.repositories.CommentRepositoryConfiguration;
import io.github.habatoo.configurations.repositories.PostRepositoryConfiguration;
import io.github.habatoo.configurations.services.ServiceTestConfiguration;
import io.github.habatoo.dto.request.PostCreateRequestDto;
import io.github.habatoo.dto.request.PostRequestDto;
import io.github.habatoo.dto.response.PostListResponseDto;
import io.github.habatoo.dto.response.PostResponseDto;
import io.github.habatoo.repositories.PostRepository;
import io.github.habatoo.repositories.mapper.PostListRowMapper;
import io.github.habatoo.service.CommentService;
import io.github.habatoo.service.FileStorageService;
import io.github.habatoo.service.PostService;
import io.github.habatoo.utils.TestDataProvider;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты PostServiceImpl.
 * <p>
 * Покрывают бизнес-логику получения постов с фильтрацией и пагинацией,
 * создание, обновление, удаление постов, работу кэша,
 * а также инкремент и декремент счетчиков лайков и комментариев.
 */
@SpringJUnitConfig(classes = {
        TestDataSourceConfiguration.class,
        CommentRepositoryConfiguration.class,
        PostRepositoryConfiguration.class,
        ServiceTestConfiguration.class})
@Transactional
@DisplayName("Интеграционные тесты PostServiceImpl")
class PostServiceIntegrationTest extends TestDataProvider {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @Value("${app.upload.dir:uploads/posts/}")
    private String uploadDir;

    @BeforeEach
    void setUp() {
        flyway.clean();
        flyway.migrate();
        preparePostAndComments(postService, commentService);
    }

    /**
     * Проверяет получение списка постов с поиском, тегами и разными размерами страниц.
     * Фильтрация по "Spring" вынесена в отдельную функцию.
     */
    @ParameterizedTest(name = "search=\"{0}\", page={1}, size={2}")
    @MethodSource("provideAllParams")
    @DisplayName("Валидация поиска и пагинации постов (размеры страницы: 1,5,10,20,50)")
    void getPostsFullTest(String search, int pageNumber, int pageSize) {
        PostListResponseDto response = postService.getPosts(search, pageNumber, pageSize);

        assertThat(response.posts()).hasSizeLessThanOrEqualTo(pageSize);
        if (!search.isBlank()) {
            assertThat(response.posts()).allMatch(this::matchesSpring);
        }
        if (pageNumber == 1) {
            assertThat(response.hasPrev()).isFalse();
        } else {
            assertThat(response.hasPrev()).isTrue();
        }

        int totalFiltered = (int) TEST_POSTS.stream()
                .filter(p -> search.isBlank() || matchesSpring(toPostResponseDto(p)))
                .count();
        int maxPage = (int) Math.ceil((double) totalFiltered / pageSize);
        if (pageNumber < maxPage) {
            assertThat(response.hasNext()).isTrue();
        } else {
            assertThat(response.hasNext()).isFalse();
        }
    }

    @Autowired
    public PostListRowMapper postListRowMapper;

    /**
     * Проверяет корректное получение поста по ID из кэша.
     */
    @Test
    @DisplayName("Получение поста по ID из кеша")
    void testGetPostByIdTest() {
        var r = jdbcTemplate.queryForObject(
                """
                        SELECT id, title, text, likes_count, comments_count
                        FROM post
                        WHERE id = ?
                        """,
                postListRowMapper,
                1L
        );
        Optional<PostResponseDto> maybePost = postService.getPostById(1L);

        assertThat(maybePost).isPresent();
        PostResponseDto post = maybePost.get();
        assertThat(post.id()).isEqualTo(1L);
        assertThat(post.tags()).contains("java");
        assertThat(post.tags()).contains("spring");
        assertThat(post.tags()).contains("programming");
    }

    /**
     * Проверяет создание нового поста и обновление кэша.
     */
    @Test
    @DisplayName("Создание нового поста с обновлением кэша")
    void testCreatePostTest() {
        PostCreateRequestDto createRequest = new PostCreateRequestDto("New Title", "New text", List.of("newtag"));
        PostResponseDto createdPost = postService.createPost(createRequest);

        assertThat(createdPost.id()).isPositive();
        assertThat(createdPost.title()).isEqualTo("New Title");
        assertThat(createdPost.tags()).contains("newtag");

        Optional<PostResponseDto> cached = postService.getPostById(createdPost.id());
        assertThat(cached).isPresent();
    }

    /**
     * Проверяет обновление существующего поста и обновление записи в кэше.
     */
    @Test
    @DisplayName("Обновление поста с обновлением кэша")
    void testUpdatePostTest() {
        PostRequestDto updateRequest = new PostRequestDto(1L, "Updated Title", "Updated Text", List.of("tag0"));
        PostResponseDto updated = postService.updatePost(updateRequest);

        assertThat(updated.title()).isEqualTo("Updated Title");
        assertThat(updated.text()).isEqualTo("Updated Text");
        assertThat(updated.tags()).contains("tag0");

        Optional<PostResponseDto> cached = postService.getPostById(1L);
        assertThat(cached).isPresent();
        assertThat(cached.get().title()).isEqualTo("Updated Title");
    }

    /**
     * Проверяет удаление поста и очистку кэша, а также удаление директории файла.
     */
    @Test
    @DisplayName("Удаление поста, очистка кэша и удаление директории")
    void testDeletePostTest() throws IOException {
        long deleteId = 2L;

        postService.deletePost(deleteId);

        Optional<PostResponseDto> cached = postService.getPostById(deleteId);
        assertThat(cached).isEmpty();

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM post WHERE id = ?", Integer.class, deleteId);
        assertThat(count).isZero();

        Path baseUploadPath = Paths.get(uploadDir).normalize().toAbsolutePath();
        Path dirPath = baseUploadPath.resolve(String.valueOf(deleteId));
        assertThat(Files.notExists(dirPath)).isTrue();

        deleteDirectoryRecursively();
    }

    /**
     * Проверяет корректное инкрементирование лайков и обновление кэша.
     */
    @Test
    @DisplayName("Инкремент лайков с обновлением кэша")
    void testIncrementLikesTest() {
        int oldLikes = postService.getPostById(1L).map(PostResponseDto::likesCount).orElse(0);
        int newLikes = postService.incrementLikes(1L);

        assertThat(newLikes).isEqualTo(oldLikes + 1);

        Optional<PostResponseDto> cached = postService.getPostById(1L);
        assertThat(cached).isPresent();
        assertThat(cached.get().likesCount()).isEqualTo(newLikes);
    }

    /**
     * Проверяет инкремент счётчика комментариев и обновление кэша.
     */
    @Test
    @DisplayName("Инкремент комментариев с обновлением кэша")
    void testIncrementCommentsCountTest() {
        int before = postService.getPostById(1L).map(PostResponseDto::commentsCount).orElse(0);
        postService.incrementCommentsCount(1L);
        int after = postService.getPostById(1L).map(PostResponseDto::commentsCount).orElse(0);

        assertThat(after).isEqualTo(before + 1);
    }

    /**
     * Проверяет декремент счётчика комментариев (не опускается ниже 0) и обновление кэша.
     */
    @Test
    @DisplayName("Декремент комментариев с обновлением кэша, не ниже 0")
    void testDecrementCommentsCountTest() {
        postService.incrementCommentsCount(1L);

        int before = postService.getPostById(1L).map(PostResponseDto::commentsCount).orElse(0);
        postService.decrementCommentsCount(1L);
        int after = postService.getPostById(1L).map(PostResponseDto::commentsCount).orElse(0);

        assertThat(after).isEqualTo(Math.max(0, before - 1));
    }

    /**
     * Проверяет корректную работу метода проверки существования поста.
     */
//    @Test
//    @DisplayName("Проверка существования поста")
//    void testPostExistsTest() {
//        assertThat(postService.postExists(1L)).isTrue();
//        assertThat(postService.postExists(999L)).isFalse();
//    }

    /**
     * Удаление тестовых директорий.
     *
     * @throws IOException при невозможности удалить
     */
    private void deleteDirectoryRecursively() throws IOException {
        Path path = Paths.get("uploads").normalize().toAbsolutePath();

        if (Files.exists(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    /**
     * Преобразование PostCreateRequestDto → PostResponseDto для тестовых данных
     */
    private PostResponseDto toPostResponseDto(PostCreateRequestDto request) {
        return new PostResponseDto(
                1L, // Fake id
                request.title(),
                request.text(),
                request.tags(),
                0,
                0);
    }

    /**
     * Фильтрация для поиска "Spring"
     */
    private boolean matchesSpring(PostResponseDto p) {
        return (p.title().contains("Spring") || p.text().contains("Spring")) && p.tags().contains("spring");
    }
}
