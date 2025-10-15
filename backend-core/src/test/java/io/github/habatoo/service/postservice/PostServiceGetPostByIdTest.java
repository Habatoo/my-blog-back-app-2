package io.github.habatoo.service.postservice;

import io.github.habatoo.dto.response.PostResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты метода getPostById класса PostServiceImpl
 */
@DisplayName("Тесты метода getPostById")
class PostServiceGetPostByIdTest extends PostServiceTestBase {

    @Test
    @DisplayName("Должен вернуть пост из кеша, если он существует")
    void shouldReturnPostIfExistsTest() {
        Optional<PostResponseDto> result = postService.getPostById(VALID_POST_ID);

        assertTrue(result.isPresent());
        assertEquals(POST_RESPONSE_1, result.get());
    }

    @Test
    @DisplayName("Должен вернуть пустой Optional если пост отсутствует")
    void shouldReturnEmptyIfPostNotExistsTest() {
        Optional<PostResponseDto> result = postService.getPostById(INVALID_POST_ID);
        assertTrue(result.isEmpty());
    }
}
