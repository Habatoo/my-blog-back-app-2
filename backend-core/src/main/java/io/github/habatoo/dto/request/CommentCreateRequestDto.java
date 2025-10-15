package io.github.habatoo.dto.request;

/**
 * DTO для запроса создания комментария.
 * <p>
 * Содержит данные для создания комментария к посту.
 * Используется как входящие данные для API endpoints создания комментариев.
 * </p>
 *
 * @param postId идентификатор поста, к которому добавляется комментарий (обязательное поле, не может быть null)
 * @param text   текст комментария (обязательное поле, не может быть null или пустым)
 */
public record CommentCreateRequestDto(
        Long postId,
        String text

) {
}
