package io.github.habatoo.controllers.post;

import io.github.habatoo.dto.request.PostCreateRequestDto;
import io.github.habatoo.dto.response.PostResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты обработки создания постов.
 */
@DisplayName("Тесты метода createPost для обработки создания постов.")
class PostControllerCreatePostTest extends PostControllerTestBase {

    @Test
    @DisplayName("Должен создать пост и вернуть 201 статус")
    void shouldCreatePostAndReturnCreatedStatusTest() {
        PostCreateRequestDto createRequest = createPostCreateRequest(POST_TITLE, POST_TEXT, POST_TAGS);
        PostResponseDto expectedResponse = createPostResponse(3L, POST_TITLE, POST_TEXT,
                POST_TAGS, 0, 0);

        when(postService.createPost(createRequest)).thenReturn(expectedResponse);

        ResponseEntity<PostResponseDto> response = postController.createPost(createRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        assertEquals(0, response.getBody().likesCount());
        assertEquals(0, response.getBody().commentsCount());
        verify(postService).createPost(createRequest);
    }

    @DisplayName("Должен создать пост с различными тегами")
    @ParameterizedTest
    @MethodSource("provideDifferentTags")
    void shouldCreatePostWithDifferentTagsTest(List<String> tags) {
        PostCreateRequestDto createRequest = createPostCreateRequest(POST_TITLE, POST_TEXT, tags);
        PostResponseDto expectedResponse = createPostResponse(1L, POST_TITLE, POST_TEXT, tags, 0, 0);

        when(postService.createPost(createRequest)).thenReturn(expectedResponse);

        ResponseEntity<PostResponseDto> response = postController.createPost(createRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(tags, response.getBody().tags());
        verify(postService).createPost(createRequest);
    }

    private static Stream<Arguments> provideDifferentTags() {
        return Stream.of(
                Arguments.of(List.of()),
                Arguments.of(List.of("один")),
                Arguments.of(List.of("tag1", "tag2")),
                Arguments.of(List.of("tag1", "tag2", "tag3", "tag4"))
        );
    }

}
