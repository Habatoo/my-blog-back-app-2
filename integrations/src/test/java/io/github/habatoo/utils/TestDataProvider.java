package io.github.habatoo.utils;

import io.github.habatoo.dto.request.CommentCreateRequestDto;
import io.github.habatoo.dto.request.PostCreateRequestDto;
import io.github.habatoo.service.CommentService;
import io.github.habatoo.service.PostService;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Stream;

/**
 * Утилита для подготовки тестовых данных — постов и комментариев — для интеграционных тестов.
 * Все записи создаются с использованием циклов на основе коллекций.
 */
public abstract class TestDataProvider {

    public static final List<PostCreateRequestDto> TEST_POSTS = List.of(
            new PostCreateRequestDto(
                    "Мой первый пост о Java",
                    "Изучаю Java и Spring Framework. Очень интересная технология!",
                    List.of("java", "spring", "programming")),
            new PostCreateRequestDto(
                    "Spring Boot преимущества",
                    "Spring Boot упрощает разработку приложений. Автоконфигурация - это круто!",
                    List.of("java", "spring")),
            new PostCreateRequestDto(
                    "Работа с базами данных",
                    "Рассказываю о основах работы с PostgreSQL и H2 в Spring приложениях. И еще добиваем число символов больше 128 и проверяем разные символы 1234567890!\"№;%:?*()_/{}[]",
                    List.of("java", "database", "tutorial", "spring")),
            new PostCreateRequestDto(
                    "Советы по программированию",
                    "Несколько полезных советов для начинающих разработчиков.",
                    List.of("programming", "tutorial")),
            new PostCreateRequestDto(
                    "Без тегов пример",
                    "Этот пост создан без тегов для демонстрации.",
                    List.of())
    );

    private static final List<CommentCreateRequestDto> COMMENTS = List.of(
            new CommentCreateRequestDto(1L, "Отличный первый пост! Удачи в изучении Java!"),
            new CommentCreateRequestDto(1L, "Spring Framework действительно мощный инструмент."),
            new CommentCreateRequestDto(2L, "Spring Boot экономит так много времени!"),
            new CommentCreateRequestDto(2L, "Можно пример настройки автоконфигурации?"),
            new CommentCreateRequestDto(2L, "Спасибо за полезную информацию!"),
            new CommentCreateRequestDto(3L, "Хорошее объяснение основ работы с БД.")
    );

    /**
     * Загружает посты в систему через циклы.
     */
    protected void preparePosts(PostService postService) {
        for (PostCreateRequestDto post : TEST_POSTS) {
            postService.createPost(post);
        }
    }

    /**
     * Загружает комментарии в систему через циклы.
     */
    protected void prepareComments(CommentService commentService) {
        for (CommentCreateRequestDto comment : COMMENTS) {
            commentService.createComment(comment);
        }
    }

    /**
     * Загружает посты и комментарии в систему.
     */
    protected void preparePostAndComments(PostService postService, CommentService commentService) {
        preparePosts(postService);
        prepareComments(commentService);
    }

    /**
     * Загружает посты с полными данными для теста изображений и ссылок к ним.
     */
    protected void preparePostsWithImages(JdbcTemplate jdbcTemplate) {
        for (long id = 1; id <= 4; id++) {
            jdbcTemplate.update(
                    "INSERT INTO post (id, title, text, likes_count, comments_count, image_url, image_name, image_size, created_at, updated_at) " +
                            "VALUES (?, ?, ?, 0, 0, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                    id,
                    "Тестовый пост " + id,
                    "Содержимое " + id,
                    "/images/img" + id + ".jpg",
                    "original_img" + id + ".jpg",
                    10000 + id * 1000
            );
        }
    }

    public static Stream<Arguments> provideAllParams() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of("", 1, 1),
                org.junit.jupiter.params.provider.Arguments.of("", 1, 5),
                org.junit.jupiter.params.provider.Arguments.of("", 1, 10),
                org.junit.jupiter.params.provider.Arguments.of("", 1, 20),
                org.junit.jupiter.params.provider.Arguments.of("", 1, 50),
                org.junit.jupiter.params.provider.Arguments.of("", 2, 1),
                org.junit.jupiter.params.provider.Arguments.of("", 2, 5),

                org.junit.jupiter.params.provider.Arguments.of("Spring #spring", 1, 1),
                org.junit.jupiter.params.provider.Arguments.of("Spring #spring", 1, 2),
                org.junit.jupiter.params.provider.Arguments.of("Spring #spring", 1, 5),
                org.junit.jupiter.params.provider.Arguments.of("Spring #spring", 2, 1),
                org.junit.jupiter.params.provider.Arguments.of("Spring #spring", 2, 2),
                org.junit.jupiter.params.provider.Arguments.of("Spring #spring", 2, 5)
        );
    }
}

