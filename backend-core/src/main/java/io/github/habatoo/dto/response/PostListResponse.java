package io.github.habatoo.dto.response;

import java.util.List;

/**
 * DTO для ответа со списком постов.
 *
 * @param posts    список постов.
 * @param hasPrev  наличие предыдущей станицы.
 * @param hasNext  наличие следующей страницы.
 * @param lastPage последняя страница номер
 */
public record PostListResponse(
        List<PostResponse> posts,
        boolean hasPrev,
        boolean hasNext,
        int lastPage) {
}
