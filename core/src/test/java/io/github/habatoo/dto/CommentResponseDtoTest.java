package io.github.habatoo.dto;

import io.github.habatoo.dto.response.CommentResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Юнит-тесты для {@link CommentResponseDto}.
 * <p>
 * Класс проверяет, что валидатор конструктора корректно выбрасывает исключения
 * при передаче некорректных значений, а также создаёт объекты с корректными параметрами.
 */
@DisplayName("Тесты для CommentResponseDto: проверка валидации конструктора")
class CommentResponseDtoTest {

    /**
     * Проверяет, что при передаче null в параметр id конструктор выбрасывает IllegalArgumentException.
     */
    @Test
    @DisplayName("Конструктор выбрасывает при null id")
    void constructorThrowsIfIdNull() {
        assertThatThrownBy(() -> new CommentResponseDto(null, "Valid text", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment ID cannot be null");
    }

    /**
     * Проверяет, что при передаче null в параметр text конструктор выбрасывает IllegalArgumentException.
     */
    @Test
    @DisplayName("Конструктор выбрасывает при null text")
    void constructorThrowsIfTextNull() {
        assertThatThrownBy(() -> new CommentResponseDto(1L, null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment text cannot be null or empty");
    }

    /**
     * Проверяет, что при передаче пустой или содержащей только пробелы строки в параметр text
     * конструктор выбрасывает IllegalArgumentException.
     */
    @Test
    @DisplayName("Конструктор выбрасывает при пустом или пробельном text")
    void constructorThrowsIfTextEmptyOrBlank() {
        assertThatThrownBy(() -> new CommentResponseDto(1L, "", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment text cannot be null or empty");

        assertThatThrownBy(() -> new CommentResponseDto(1L, "   ", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment text cannot be null or empty");
    }

    /**
     * Проверяет, что при передаче null в параметр postId конструктор выбрасывает IllegalArgumentException.
     */
    @Test
    @DisplayName("Конструктор выбрасывает при null postId")
    void constructorThrowsIfPostIdNull() {
        assertThatThrownBy(() -> new CommentResponseDto(1L, "Valid text", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Post ID cannot be null");
    }

    /**
     * Проверяет, что конструктор успешно создаёт объект при передаче корректных параметров.
     */
    @Test
    @DisplayName("Конструктор успешно создаёт объект с валидными параметрами")
    void constructorSucceedsWithValidParameters() {
        CommentResponseDto dto = new CommentResponseDto(1L, "Valid text", 10L);
    }
}
