package io.github.habatoo.dto.request;

import java.util.List;

/**
 * DTO для запроса обновления поста.
 * <p>
 * Содержит данные для обновления существующего поста.
 * Используется как входящие данные для API endpoints редактирования постов.
 * </p>
 *
 * @param id    уникальный идентификатор поста (обязателен для обновления)
 * @param title название поста (обязательное поле)
 * @param text  текст поста в формате Markdown (обязательное поле)
 * @param tags  список тегов поста (не может быть null)
 */
public record PostRequestDto(
        Long id,
        String title,
        String text,
        List<String> tags
) {
}
