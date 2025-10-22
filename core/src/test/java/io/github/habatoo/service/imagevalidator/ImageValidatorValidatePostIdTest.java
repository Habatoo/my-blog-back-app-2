package io.github.habatoo.service.imagevalidator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Набор параметризованных тестов для проверки валидации postId методом validatePostId(Long).
 * Тесты убеждаются, что корректные значения не вызывают исключений,
 * а некорректные (0, отрицательные, null) выбрасывают {@link IllegalStateException}.
 */
@DisplayName("Тесты метода validatePostId в ImageValidatorImpl")
class ImageValidatorValidatePostIdTest extends ImageValidatorTestBase {

    /**
     * Проверяет, что метод не выбрасывает исключение для валидных (положительных) postId.
     * @param postId валидный идентификатор поста (больше 0)
     */
    @ParameterizedTest
    @ValueSource(longs = {1, 2, 100, Long.MAX_VALUE})
    @DisplayName("Не должен выбрасывать исключение для валидного postId")
    void shouldNotThrowForValidPostIdTest(Long postId) {
        assertDoesNotThrow(() -> imageValidator.validatePostId(postId));
    }

    /**
     * Проверяет, что для невалидного (нулевого или отрицательного) postId метод выбрасывает IllegalStateException.
     * @param postId невалидный идентификатор поста (0 или меньше)
     */
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -999, Long.MIN_VALUE})
    @DisplayName("Должен выбрасывать исключение для невалидного postId")
    void shouldThrowForInvalidPostIdTest(Long postId) {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> imageValidator.validatePostId(postId));
        assertTrue(ex.getMessage().startsWith("Invalid postId"));
    }

    /**
     * Проверяет, что при передаче null postId выбрасывается IllegalStateException.
     * @param postId всегда null для этой проверки
     */
    @ParameterizedTest
    @NullSource
    @DisplayName("Должен выбрасывать исключение для null postId")
    void shouldThrowForNullPostIdTest(Long postId) {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> imageValidator.validatePostId(postId));
        assertTrue(ex.getMessage().startsWith("Invalid postId"));
    }
}
