package io.github.habatoo.service.postservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Тесты метода postExists класса PostServiceImpl
 */
@DisplayName("Тесты метода postExists")
class PostServicePostExistsTest extends PostServiceTestBase {

    @ParameterizedTest(name = "Пост с id {0} существует? {1}")
    @MethodSource("postIdProvider")
    void shouldReturnCorrectExistenceTest(Long postId, boolean exists) {
        boolean result = postService.postExists(postId);
        assertEquals(exists, result);
    }
}

