package io.github.habatoo.services;

import io.github.habatoo.configurations.TestDataSourceConfiguration;
import io.github.habatoo.configurations.repositories.CommentRepositoryConfiguration;
import io.github.habatoo.configurations.repositories.PostRepositoryConfiguration;
import io.github.habatoo.configurations.services.ServiceTestConfiguration;
import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.service.PostService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringJUnitConfig(classes = {
        TestDataSourceConfiguration.class,
        ServiceTestConfiguration.class,
        PostRepositoryConfiguration.class,
        CommentRepositoryConfiguration.class})
@DisplayName("Интеграционные тесты PostServiceImpl")
class PostServiceIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clean() {
        jdbcTemplate.execute("DELETE FROM post_tag");
        jdbcTemplate.execute("DELETE FROM tag");
        jdbcTemplate.execute("DELETE FROM post");
    }

    @Test
    void testCreateAndFindPost() {
        PostCreateRequest req = new PostCreateRequest("Тест-пост", "Контент поста", List.of("тег1", "тег2"));
        PostResponse created = postService.createPost(req);

        assertThat(created.id()).isPositive();
        assertThat(created.title()).isEqualTo("Тест-пост");
        assertThat(created.tags()).contains("тег1", "тег2");

        Optional<PostResponse> found = postService.getPostById(created.id());
        assertTrue(found.isPresent());
        assertThat(found.get().title().contains("Тест-пост"));
    }

    @Test
    void testUpdatePost() {
        PostCreateRequest req = new PostCreateRequest("Тест", "Конт.", List.of("A"));
        PostResponse created = postService.createPost(req);

        PostRequest updReq = new PostRequest(created.id(), "Обновлено", "Новый текст", List.of("A", "B"));
        PostResponse updated = postService.updatePost(updReq);

        assertThat(updated.title()).isEqualTo("Обновлено");
        assertThat(updated.tags()).contains("A", "B");
    }
}

