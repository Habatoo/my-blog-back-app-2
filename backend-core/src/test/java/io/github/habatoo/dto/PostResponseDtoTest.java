package io.github.habatoo.dto;

import io.github.habatoo.dto.response.PostResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Юнит-тесты для {@link PostResponseDto}.
 * <p>
 * Проверяют корректность валидаций конструктора,
 * в том числе обязательных полей и корректной инициализации значений.
 */
@DisplayName("Тесты для PostResponseDto: проверка валидации конструктора и инициализации")
class PostResponseDtoTest {

    /**
     * Проверяет, что конструктор выбрасывает исключение, если id равен null.
     */
    @Test
    @DisplayName("Конструктор выбрасывает при null id")
    void constructorThrowsIfIdNull() {
        assertThatThrownBy(() -> new PostResponseDto(null, "Some title", "Some text", List.of("tag1"), 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Post ID cannot be null");
    }

    /**
     * Проверяет, что конструктор выбрасывает исключение, если title равен null или пуст.
     */
    @Test
    @DisplayName("Конструктор выбрасывает при null или пустом title")
    void constructorThrowsIfTitleNullOrEmpty() {
        assertThatThrownBy(() -> new PostResponseDto(1L, null, "Some text", List.of("tag1"), 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Post title cannot be null or empty");

        assertThatThrownBy(() -> new PostResponseDto(1L, "   ", "Some text", List.of("tag1"), 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Post title cannot be null or empty");
    }

    /**
     * Проверяет, что конструктор выбрасывает исключение, если text равен null или пуст.
     */
    @Test
    @DisplayName("Конструктор выбрасывает при null или пустом text")
    void constructorThrowsIfTextNullOrEmpty() {
        assertThatThrownBy(() -> new PostResponseDto(1L, "Title", null, List.of("tag1"), 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Post text cannot be null or empty");

        assertThatThrownBy(() -> new PostResponseDto(1L, "Title", "   ", List.of("tag1"), 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Post text cannot be null or empty");
    }

    /**
     * Проверяет, что конструктор выбрасывает исключение, если tags равен null.
     */
    @Test
    @DisplayName("Конструктор выбрасывает при null tags")
    void constructorThrowsIfTagsNull() {
        assertThatThrownBy(() -> new PostResponseDto(1L, "Title", "Text", null, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Post tags cannot be null");
    }

    /**
     * Проверяет, что likesCount и commentsCount устанавливаются в 0, если переданы null или отрицательные значения.
     */
    @Test
    @DisplayName("Конструктор корректно обрабатывает null и отрицательные likesCount и commentsCount")
    void constructorHandlesNullAndNegativeCounts() {
        PostResponseDto dto1 = new PostResponseDto(1L, "Title", "Text", List.of("tag1"), null, null);
        assert dto1.likesCount() == 0;
        assert dto1.commentsCount() == 0;

        PostResponseDto dto2 = new PostResponseDto(1L, "Title", "Text", List.of("tag1"), -5, -2);
        assert dto2.likesCount() == 0;
        assert dto2.commentsCount() == 0;
    }

    /**
     * Проверяет успешное создание объекта с валидными параметрами и неизменяемость списка tags.
     */
    @Test
    @DisplayName("Конструктор успешно создаёт объект с валидными параметрами")
    void constructorSucceedsWithValidParameters() {
        List<String> tags = List.of("tag1", "tag2");
        PostResponseDto dto = new PostResponseDto(1L, "Title", "Text", tags, 5, 3);

        assert dto.id() == 1L;
        assert dto.title().equals("Title");
        assert dto.text().equals("Text");
        assert dto.tags().size() == tags.size();
        assert dto.likesCount() == 5;
        assert dto.commentsCount() == 3;
    }
}
