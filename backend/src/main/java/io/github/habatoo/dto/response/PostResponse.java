package io.github.habatoo.dto.response;

import java.util.List;

/**
 * DTO для ответа с данными поста.
 * <p>
 * Содержит данные для отображения поста в API ответах.
 * </p>
 *
 * @param id            сгенерированный идентификатор поста
 * @param title         название поста
 * @param text          текст поста в формате Markdown
 * @param tags          список тегов поста
 * @param likesCount    количество лайков поста
 * @param commentsCount количество комментариев поста
 */
public record PostResponse(
        Long id,
        String title,
        String text,
        List<String> tags,
        Integer likesCount,
        Integer commentsCount
) {

    /**
     * Конструктор с валидацией обязательных полей.
     */
    public PostResponse {
        if (id == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Post title cannot be null or empty");
        }
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Post text cannot be null or empty");
        }
        if (tags == null) {
            throw new IllegalArgumentException("Post tags cannot be null");
        }
        if (likesCount == null || likesCount < 0) {
            likesCount = 0;
        }
        if (commentsCount == null || commentsCount < 0) {
            commentsCount = 0;
        }

        tags = List.copyOf(tags);
    }
}
