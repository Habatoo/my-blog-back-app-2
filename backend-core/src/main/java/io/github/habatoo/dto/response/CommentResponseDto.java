package io.github.habatoo.dto.response;

/**
 * DTO для ответа с данными комментария.
 * <p>
 * Содержит все необходимые данные для отображения комментария в API ответах.
 * </p>
 *
 * @param id     уникальный идентификатор комментария
 * @param text   текст комментария
 * @param postId идентификатор связанного поста
 */
public record CommentResponseDto(
        Long id,
        String text,
        Long postId
) {

    /**
     * Конструктор с валидацией обязательных полей.
     */
    public CommentResponseDto {
        if (id == null) {
            throw new IllegalArgumentException("Comment ID cannot be null");
        }
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment text cannot be null or empty");
        }
        if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }
    }
}
