package io.github.habatoo.dto.request;

import java.util.List;

/**
 * DTO для запроса создания поста.
 * <p>
 * Содержит данные для создания нового поста.
 * Используется как входящие данные для API endpoints создания постов.
 * </p>
 *
 * @param title название поста (обязательное поле)
 * @param text  текст поста в формате Markdown (обязательное поле)
 * @param tags  список тегов поста (не может быть null)
 */
public record PostCreateRequestDto(
        String title,
        String text,
        List<String> tags
) {
}
