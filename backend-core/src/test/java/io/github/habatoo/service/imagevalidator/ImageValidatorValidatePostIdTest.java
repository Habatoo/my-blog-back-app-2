package io.github.habatoo.service.imagevalidator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты метода validatePostId в ImageValidatorImpl")
class ImageValidatorValidatePostIdTest extends ImageValidatorTestBase {

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 100, Long.MAX_VALUE})
    @DisplayName("Не должен выбрасывать исключение для валидного postId")
    void shouldNotThrowForValidPostIdTest(Long postId) {
        assertDoesNotThrow(() -> imageValidator.validatePostId(postId));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -999, Long.MIN_VALUE})
    @DisplayName("Должен выбрасывать исключение для невалидного postId")
    void shouldThrowForInvalidPostIdTest(Long postId) {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> imageValidator.validatePostId(postId));
        assertTrue(ex.getMessage().startsWith("Invalid postId"));
    }

    @Test
    @DisplayName("Должен выбрасывать исключение для null postId")
    void shouldThrowForNullPostIdTest() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> imageValidator.validatePostId(null));
        assertTrue(ex.getMessage().startsWith("Invalid postId"));
    }
}
