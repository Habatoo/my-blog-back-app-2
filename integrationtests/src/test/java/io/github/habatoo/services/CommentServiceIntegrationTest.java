package io.github.habatoo.services;

import io.github.habatoo.configurations.TestDataSourceConfiguration;
import io.github.habatoo.configurations.repositories.CommentRepositoryConfiguration;
import io.github.habatoo.configurations.repositories.PostRepositoryConfiguration;
import io.github.habatoo.configurations.services.ServiceTestConfiguration;
import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.repositories.PostRepository;
import io.github.habatoo.service.CommentService;
import io.github.habatoo.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringJUnitConfig(classes = {
        TestDataSourceConfiguration.class,
        ServiceTestConfiguration.class,
        PostRepositoryConfiguration.class,
        CommentRepositoryConfiguration.class})
@DisplayName("Интеграционные тесты CommentServiceImpl")
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepareData() {
        jdbcTemplate.execute("DELETE FROM comment");
        jdbcTemplate.execute("DELETE FROM post");
        jdbcTemplate.update("""
                INSERT INTO post (id, title, text, likes_count, comments_count, created_at, updated_at)
                VALUES (1, 'Сервисный пост', 'Контент', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) 
                """);
       // postService.createPost()
    }

    @Test
    @DisplayName("Создание комментария через сервис и получение по посту")
    void testCreateAndGetComments() {
        CommentCreateRequest req = new CommentCreateRequest(1L, "Новый комментарий");
        var r = postRepository.findAllPosts();
        CommentResponse saved = commentService.createComment(req);

        assertThat(saved.id()).isPositive();
        assertThat(saved.text()).isEqualTo("Новый комментарий");
        List<CommentResponse> comments = commentService.getCommentsByPostId(1L);

        assertThat(comments).isNotEmpty();
        assertThat(comments).anyMatch(c -> c.text().equals("Новый комментарий"));
    }

    @Test
    @DisplayName("Удаление комментария через сервис уменьшает счётчик в посте")
    void testDeleteComment() {
        CommentCreateRequest req = new CommentCreateRequest(1L, "Удаляемый комментарий");
        CommentResponse saved = commentService.createComment(req);

        Integer before = jdbcTemplate.queryForObject("SELECT comments_count FROM post WHERE id = ?", Integer.class, 1L);
        commentService.deleteComment(1L, saved.id());
        Integer after = jdbcTemplate.queryForObject("SELECT comments_count FROM post WHERE id = ?", Integer.class, 1L);

        assertThat(after).isEqualTo(before - 1);
        assertThat(commentService.getCommentsByPostId(1L)).allMatch(c -> !c.id().equals(saved.id()));
    }
}
