package io.github.habatoo.repository;

import io.github.habatoo.contfiguration.TestConfiguration;
import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.repository.impl.CommentRepositoryImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = {TestConfiguration.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommentRepositoryIntegrationTest {

    @Autowired
    private CommentRepositoryImpl commentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    void setupSchema() {
        jdbcTemplate.execute("CREATE TABLE comments (id BIGINT AUTO_INCREMENT PRIMARY KEY, post_id BIGINT, text VARCHAR(256), created_at TIMESTAMP, updated_at TIMESTAMP)");
        jdbcTemplate.update("INSERT INTO comments (post_id, text, created_at, updated_at) VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", 1L, "Первый тестовый комментарий");
    }

    @Test
    void testFindByPostId() {
        List<CommentResponse> comments = commentRepository.findByPostId(1L);
        assertFalse(comments.isEmpty());
        assertEquals("Первый тестовый комментарий", comments.get(0).text());
    }

    @Test
    void testFindByPostIdAndId() {
        Optional<CommentResponse> comment = commentRepository.findByPostIdAndId(1L, 1L);
        assertTrue(comment.isPresent());
        assertEquals("Первый тестовый комментарий", comment.get().text());
    }

    @Test
    void testSaveAndFind() {
        CommentCreateRequest newRequest = new CommentCreateRequest("Новый интеграционный комментарий", 1L);
        CommentResponse created = commentRepository.save(newRequest);
        assertEquals("Новый интеграционный комментарий", created.text());

        Optional<CommentResponse> found = commentRepository.findByPostIdAndId(1L, created.id());
        assertTrue(found.isPresent());
        assertEquals("Новый интеграционный комментарий", found.get().text());
    }

    @Test
    void testUpdateText() {
        CommentResponse updated = commentRepository.updateText(1L, "Изменённый текст для комментария");
        assertEquals("Изменённый текст для комментария", updated.text());
    }

    @Test
    void testDeleteById() {
        int deleted = commentRepository.deleteById(1L);
        assertEquals(1, deleted);
        List<CommentResponse> comments = commentRepository.findByPostId(1L);
        assertTrue(comments.isEmpty() || comments.stream().noneMatch(c -> c.id() == 1L));
    }
}
