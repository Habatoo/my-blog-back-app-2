package io.github.habatoo.service.dto;

/**
 * Record для данных пагинации
 */
public record PaginationData(boolean hasPrev, boolean hasNext, int lastPage) {
}
