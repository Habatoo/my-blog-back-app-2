package io.github.habatoo.service.postservice;

import io.github.habatoo.dto.request.PostCreateRequestDto;
import io.github.habatoo.dto.request.PostRequestDto;
import io.github.habatoo.dto.response.PostResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты методов createPost и updatePost класса PostServiceImpl
 */
@DisplayName("Тесты методов createPost и updatePost")
class PostServiceCreateUpdatePostTest extends PostServiceTestBase {

    @ParameterizedTest(name = "Метод: {0}, успешное выполнение: {1}")
    @MethodSource("provideCreateUpdate")
    @DisplayName("Должен корректно создавать или обновлять пост и обновлять кеш")
    void shouldCreateOrUpdatePostTest(String method, boolean success) {
        PostCreateRequestDto createRequest = new PostCreateRequestDto("Заголовок", "Текст", List.of("tag9", "tag10"));

        List<String> updatedTags = List.of("tag9_new", "tag10");
        PostRequestDto updateRequest = new PostRequestDto(VALID_POST_ID, "Заголовок обновленный", "Текст обновленный", updatedTags);
        PostResponseDto response = new PostResponseDto(VALID_POST_ID, createRequest.title(), createRequest.text(), updatedTags, 0, 0);

        if ("create".equals(method)) {
            if (success) {
                when(postRepository.createPost(createRequest)).thenReturn(response);
                PostResponseDto result = postService.createPost(createRequest);
                assertEquals(response, result);
            } else {
                when(postRepository.createPost(createRequest)).thenThrow(new RuntimeException("DB error"));
                assertThrows(IllegalStateException.class, () -> postService.createPost(createRequest));
            }
            verify(postRepository).createPost(createRequest);
        } else {
            if (success) {
                when(postRepository.updatePost(updateRequest)).thenReturn(response);
                PostResponseDto result = postService.updatePost(updateRequest);
                assertEquals(response, result);
            } else {
                when(postRepository.updatePost(updateRequest)).thenThrow(new RuntimeException("DB error"));
                assertThrows(IllegalStateException.class, () -> postService.updatePost(updateRequest));
            }
            verify(postRepository).updatePost(updateRequest);
        }
    }

    static Stream<Arguments> provideCreateUpdate() {
        return Stream.of(
                Arguments.of("create", true),
                Arguments.of("create", false),
                Arguments.of("update", true),
                Arguments.of("update", false)
        );
    }
}
