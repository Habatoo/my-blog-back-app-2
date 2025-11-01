package io.github.habatoo.service.postservice;

import io.github.habatoo.dto.response.PostResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Тесты метода getPostById класса PostServiceImpl
 */
@DisplayName("Тесты метода getPostById")
class PostServiceGetPostByIdTest extends PostServiceTestBase {

    /**
     * Проверяет, что сервис корректно возвращает Optional<PostResponseDto>, если пост существует.
     */
    @Test
    @DisplayName("Должен вернуть пост если он существует")
    void shouldReturnPostIfExistsTest() {
        when(postRepository.getPostById(VALID_POST_ID)).thenReturn(Optional.of(POST_RESPONSE_1));
        Optional<PostResponseDto> result = postService.getPostById(VALID_POST_ID);

        assertTrue(result.isPresent());
        assertEquals(POST_RESPONSE_1.id(), result.get().id());
        assertEquals(POST_RESPONSE_1.title(), result.get().title());
        assertEquals(POST_RESPONSE_1.text(), result.get().text());
    }

    /**
     * Проверяет, что при отсутствии поста метод возвращает пустой Optional без выбрасывания исключения.
     */
    @Test
    @DisplayName("Должен вернуть пустой Optional если пост отсутствует")
    void shouldReturnEmptyIfPostNotExistsTest() {
        assertDoesNotThrow(() -> postService.getPostById(INVALID_POST_ID));
    }
}
