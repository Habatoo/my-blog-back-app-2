package io.github.habatoo.repositories;

import io.github.habatoo.configurations.TestDataSourceConfiguration;
import io.github.habatoo.configurations.repositories.PostRepositoryConfiguration;
import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.repositories.impl.PostRepositoryImpl;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Интеграционные тесты для репозитория {@link PostRepositoryImpl}.
 * <p>
 * Тесты охватывают основные операции с постами:
 * создание, обновление, удаление, поиск всех постов
 * и работу с тегами. Также проверяются граничные случаи и ошибки.
 * </p>
 */
@SpringJUnitConfig(classes = {TestDataSourceConfiguration.class, PostRepositoryConfiguration.class})
@DisplayName("Интеграционные тесты PostRepository")
public class PostRepositoryIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Подготовка тестовой базы для каждого теста:
     * - Очистка и миграция схемы
     * - Вставка тестовых постов и тегов с привязками
     */
    @BeforeEach
    @DisplayName("Подготовка тестовой базы и вставка постов с тегами")
    void setup() {
        flyway.clean();
        flyway.migrate();

        for (long id = 1; id <= 3; id++) {
            jdbcTemplate.update(
                    "INSERT INTO post (id, title, text, likes_count, comments_count, image_url, image_name, image_size, created_at, updated_at) " +
                            "VALUES (?, ?, ?, 0, 0, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                    id, "Пост " + id, "Контент поста " + id);
        }

        jdbcTemplate.update("INSERT INTO tag (id, name, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)", 1L, "Tag1");
        jdbcTemplate.update("INSERT INTO tag (id, name, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)", 2L, "Tag2");

        jdbcTemplate.update("INSERT INTO post_tag (post_id, tag_id, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)", 1L, 1L);
        jdbcTemplate.update("INSERT INTO post_tag (post_id, tag_id, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)", 1L, 2L);
        jdbcTemplate.update("INSERT INTO post_tag (post_id, tag_id, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)", 2L, 1L);
    }

    /**
     * Получает список всех постов с их тегами.
     * <p>
     * Проверяет, что полученный список не пустой и что у постов
     * корректно загружаются связанные теги.
     * </p>
     */
    @Test
    @DisplayName("Получение всех постов с тегами")
    void testFindAllPosts() {
        List<PostResponse> posts = postRepository.findAllPosts();

        assertThat(posts).isNotEmpty();
        assertThat(posts).anyMatch(p -> p.tags().contains("Tag1"));
        assertThat(posts).anyMatch(p -> p.tags().contains("Tag2"));
    }

    /**
     * Создаёт новый пост с тегами через репозиторий и проверяет:
     * <ul>
     *     <li>Корректность созданного объекта (id, title, text, tags)</li>
     *     <li>Создание нового тега, если такого ещё не было</li>
     *     <li>Связь поста и тегов через таблицу пост_тег</li>
     * </ul>
     */
    @Test
    @DisplayName("Создание нового поста с тегами")
    void testCreatePost() {
        flyway.clean();
        flyway.migrate();
        PostCreateRequest request = new PostCreateRequest("Новый пост", "Текст нового поста", List.of("Tag1", "Tag3"));
        PostResponse created = postRepository.createPost(request);

        assertThat(created.id()).isPositive();
        assertThat(created.title()).isEqualTo("Новый пост");
        assertThat(created.text()).isEqualTo("Текст нового поста");
        assertThat(created.tags()).containsExactlyInAnyOrder("Tag1", "Tag3");

        Integer countTag3 = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tag WHERE name = ?", Integer.class, "Tag3");
        assertThat(countTag3).isOne();

        Integer countPostTags = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post_tag WHERE post_id = ?", Integer.class, created.id());
        assertThat(countPostTags).isEqualTo(2);
    }

    /**
     * Обновляет существующий пост через репозиторий и
     * проверяет корректность обновлённого заголовка и текста.
     */
    @Test
    @DisplayName("Обновление существующего поста")
    void testUpdatePost() {
        PostRequest request = new PostRequest(1L, "Обновлённый заголовок", "Обновлённый текст", List.of("Tag1", "Tag3"));
        PostResponse updated = postRepository.updatePost(request);

        assertThat(updated).isNotNull();
        assertThat(updated.id()).isEqualTo(1L);
        assertThat(updated.title()).isEqualTo("Обновлённый заголовок");
        assertThat(updated.text()).isEqualTo("Обновлённый текст");
    }

    /**
     * Удаляет существующий пост через репозиторий и проверяет,
     * что запись удалена из базы данных.
     */
    @Test
    @DisplayName("Удаление существующего поста")
    void testDeletePost_existing() {
        postRepository.deletePost(3L);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post WHERE id = ?", Integer.class, 3L);
        assertThat(count).isZero();
    }

    /**
     * Пытается удалить несуществующий пост и проверяет,
     * что выбрасывается исключение с правильным сообщением.
     */
    @Test
    @DisplayName("Удаление несуществующего поста вызывает исключение")
    void testDeletePost_nonExisting() {
        assertThatThrownBy(() -> postRepository.deletePost(999L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Пост не найден для удаления id==999");
    }

    /**
     * Получает список тегов для существующего поста и
     * проверяет корректность связки через таблицу пост_тег.
     */
    @Test
    @DisplayName("Получение тегов для существующего поста")
    void testGetTagsForPost_existing() {
        List<String> tags = postRepository.getTagsForPost(1L);
        assertThat(tags).containsExactlyInAnyOrder("Tag1", "Tag2");
    }

    /**
     * Получает список тегов для несуществующего поста и
     * проверяет, что вернулся пустой список.
     */
    @Test
    @DisplayName("Получение тегов для несуществующего поста возвращает пустой список")
    void testGetTagsForPost_nonExisting() {
        List<String> tags = postRepository.getTagsForPost(999L);
        assertThat(tags).isEmpty();
    }

    /**
     * Увеличивает количество лайков для существующего поста и
     * проверяет, что значение в базе увеличилось.
     */
    @Test
    @DisplayName("Увеличение количества лайков существующего поста")
    void testIncrementLikes_existing() {
        postRepository.incrementLikes(1L);

        Integer likesCount = jdbcTemplate.queryForObject(
                "SELECT likes_count FROM post WHERE id = ?", Integer.class, 1L);
        assertThat(likesCount).isEqualTo(1);
    }

    /**
     * Пытается увеличить количество лайков для несуществующего поста
     * и проверяет, что выбрасывается соответствующее исключение.
     */
    @Test
    @DisplayName("Увеличение количества лайков несуществующего поста вызывает исключение")
    void testIncrementLikes_nonExisting() {
        assertThatThrownBy(() -> postRepository.incrementLikes(999L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Пост не найден при увеличении лайков id=999");
    }

    /**
     * Увеличивает счётчик комментариев существующего поста и
     * проверяет, что значение увеличилось на 1.
     */
    @Test
    @DisplayName("Увеличение счётчика комментариев существующего поста")
    void testIncrementCommentsCount() {
        Integer before = jdbcTemplate.queryForObject(
                "SELECT comments_count FROM post WHERE id = ?", Integer.class, 1L);
        postRepository.incrementCommentsCount(1L);
        Integer after = jdbcTemplate.queryForObject(
                "SELECT comments_count FROM post WHERE id = ?", Integer.class, 1L);

        assertThat(after).isEqualTo(before + 1);
    }

    /**
     * Уменьшает счётчик комментариев существующего поста и
     * проверяет, что значение уменьшилось на 1.
     */
    @Test
    @DisplayName("Уменьшение счётчика комментариев существующего поста")
    void testDecrementCommentsCount() {
        postRepository.incrementCommentsCount(1L);

        Integer before = jdbcTemplate.queryForObject(
                "SELECT comments_count FROM post WHERE id = ?", Integer.class, 1L);
        postRepository.decrementCommentsCount(1L);
        Integer after = jdbcTemplate.queryForObject(
                "SELECT comments_count FROM post WHERE id = ?", Integer.class, 1L);

        assertThat(after).isEqualTo(before - 1);
    }
}
