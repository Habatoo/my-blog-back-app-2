package io.github.habatoo.dto.request;

/**
 * DTO для запроса создания комментария.
 * <p>
 * Содержит данные для создания комментария к посту.
 * Используется как входящие данные для API endpoints создания комментариев.
 * </p>
 *
 * @param text   текст комментария (обязательное поле, не может быть null или пустым)
 * @param postId идентификатор поста, к которому добавляется комментарий (обязательное поле, не может быть null)
 */
public record CommentCreateRequest(
        String text,
        Long postId
) {
}
