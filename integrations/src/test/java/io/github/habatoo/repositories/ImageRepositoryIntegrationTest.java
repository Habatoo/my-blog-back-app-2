package io.github.habatoo.repositories;

import io.github.habatoo.Application;
import io.github.habatoo.utils.TestDataProvider;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Интеграционные тесты для репозитория {@link ImageRepository} по работе с изображениями постов.
 * <p>
 * Проверяется корректность поиска имени файла изображения, обновления метаданных в основной таблице постов,
 * а также проверки существования постов. Все операции тестируются с учётом различных сценариев и граничных случаев.
 * </p>
 */
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
@DisplayName("Интеграционные тесты ImageRepository")
public class ImageRepositoryIntegrationTest extends TestDataProvider {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Подготавливает тестовую базу для каждого теста:
     * - Очищает и применяет миграции Flyway
     * - Вставляет 4 записи в таблицу post с начальными данными и метаданными для изображений
     */
    @BeforeEach
    @DisplayName("Подготовка тестовой базы и вставка тестовых постов с метаданными изображений")
    void setUp() {
        flyway.clean();
        flyway.migrate();
        preparePostsWithImages(jdbcTemplate);
    }

    /**
     * Проверяет корректный поиск имени файла изображения по существующему postId.
     */
    @Test
    @DisplayName("Поиск имени файла изображения по postId (существующее изображение)")
    void testFindImageFileNameByPostIdExistingTest() {
        Optional<String> fileName = imageRepository.findImageFileNameByPostId(1L);
        assertThat(fileName).isPresent();
        assertThat(fileName.get()).isEqualTo("original_img1.jpg");
    }

    /**
     * Проверяет, что для поста без изображения возвращается пустой Optional.
     */
    @Test
    @DisplayName("Поиск имени файла изображения по postId (изображения нет)")
    void testFindImageFileNameByPostIdNotFoundTest() {
        jdbcTemplate.update(
                "INSERT INTO post (id, title, text, likes_count, comments_count, created_at, updated_at) VALUES (?, ?, ?, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                10L, "Пост без изображения", "Контент");

        Optional<String> fileName = imageRepository.findImageFileNameByPostId(10L);
        assertThat(fileName).isEmpty();
    }

    /**
     * Проверяет успешное обновление метаданных изображения у существующего поста.
     */
    @Test
    @DisplayName("Обновление метаданных изображения существующего поста")
    void testUpdateImageMetadataExistingPostTest() {
        imageRepository.updateImageMetadata(
                1L,
                "updated_img1.jpg",
                54321L,
                "/images/updated_img1.jpg"
        );

        String updatedOriginalName = jdbcTemplate.queryForObject(
                "SELECT image_name FROM post WHERE id = ?", String.class, 1L);
        Integer updatedSize = jdbcTemplate.queryForObject(
                "SELECT image_size FROM post WHERE id = ?", Integer.class, 1L);
        String updatedUrl = jdbcTemplate.queryForObject(
                "SELECT image_url FROM post WHERE id = ?", String.class, 1L);

        assertThat(updatedOriginalName).isEqualTo("updated_img1.jpg");
        assertThat(updatedSize).isEqualTo(54321);
        assertThat(updatedUrl).isEqualTo("/images/updated_img1.jpg");
    }

    /**
     * Проверяет, что обновление метаданных изображения для несуществующего поста приводит к исключению.
     */
    @Test
    @DisplayName("Обновление метаданных изображения несуществующего поста вызывает исключение")
    void testUpdateImageMetadataNonExistingPostTest() {
        assertThatThrownBy(() -> imageRepository.updateImageMetadata(
                        999L,
                        "file.jpg",
                        123L,
                        "/images/file.jpg"
                )
        )
                .isInstanceOf(EmptyResultDataAccessException.class)
                .hasMessageContaining("Post not found");
    }

    /**
     * Проверяет, что при попытке обновления метаданных изображения несуществующего поста данные в таблице не изменились.
     */
    @Test
    @DisplayName("Обновление метаданных изображения для несуществующего поста — проверка, что данные не обновились")
    void testUpdateImageMetadataNonExistingPostNoDataUpdatedTest() {
        int countBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post WHERE image_name IS NOT NULL", Integer.class);

        try {
            imageRepository.updateImageMetadata(
                    999L,
                    "file.jpg",
                    123L,
                    "/images/file.jpg"
            );
        } catch (EmptyResultDataAccessException ignored) {
        }

        int countAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post WHERE image_name IS NOT NULL", Integer.class);

        assertThat(countAfter).isEqualTo(countBefore);
    }

    /**
     * Проверка существования поста по его идентификатору (положительный случай).
     */
    @Test
    @DisplayName("Проверка существования поста по id (существующий пост)")
    void testExistsPostByIdExistingTest() {
        assertThat(imageRepository.existsPostById(1L)).isTrue();
    }

    /**
     * Проверка существования поста по его идентификатору (отрицательный случай).
     */
    @Test
    @DisplayName("Проверка существования поста по id (несуществующий пост)")
    void testExistsPostByIdNonExistingTest() {
        assertThat(imageRepository.existsPostById(999L)).isFalse();
    }
}
