package io.github.habatoo.controllers.post;

import io.github.habatoo.dto.request.PostRequestDto;
import io.github.habatoo.dto.response.PostResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты обработки редактирования постов.
 */
@DisplayName("Тесты метода updatePost для обработки редактирования постов.")
class PostControllerUpdatePostTest extends PostControllerTestBase {

    @Test
    @DisplayName("Должен обновить пост и вернуть 200 статус")
    void shouldUpdatePostAndReturnOkStatusTest() {
        PostRequestDto updateRequest = createPostRequest(VALID_POST_ID, "Новый заголовок",
                "Новый текст", List.of("newTag"));
        PostResponseDto expectedResponse = createPostResponse(VALID_POST_ID, "Новый заголовок",
                "Новый текст", List.of("newTag"), 5, 3);

        when(postService.updatePost(updateRequest)).thenReturn(expectedResponse);

        ResponseEntity<PostResponseDto> response = postController.updatePost(VALID_POST_ID, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(postService).updatePost(updateRequest);
    }

    @DisplayName("Должен обновить пост с различными данными")
    @ParameterizedTest
    @CsvSource({
            "1, 'Заголовок 1', 'Текст 1', 'tag1,tag2'",
            "2, 'Другой заголовок', 'Длинный текст поста', ''",
            "3, 'Короткий', 'Текст', 'единственный тег'"
    })
    void shouldUpdatePostWithDifferentDataTest(Long postId, String title, String text, String tags) {
        List<String> tagList = tags.isEmpty() ? List.of() : List.of(tags.split(","));
        PostRequestDto updateRequest = createPostRequest(postId, title, text, tagList);
        PostResponseDto expectedResponse = createPostResponse(postId, title, text, tagList, 0, 0);

        when(postService.updatePost(updateRequest)).thenReturn(expectedResponse);

        ResponseEntity<PostResponseDto> response = postController.updatePost(postId, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(title, response.getBody().title());
        assertEquals(text, response.getBody().text());
        verify(postService).updatePost(updateRequest);
    }

}
