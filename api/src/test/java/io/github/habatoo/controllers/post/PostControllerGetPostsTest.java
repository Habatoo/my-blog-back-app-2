package io.github.habatoo.controllers.post;

import io.github.habatoo.dto.response.PostListResponseDto;
import io.github.habatoo.dto.response.PostResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты обработки получения постов по поиску и пагинации.
 */
class PostControllerGetPostsTest extends PostControllerTestBase {

    @Test
    @DisplayName("Должен вернуть пагинированный список постов при валидных параметрах")
    void shouldReturnPaginatedPostListWithValidParametersTest() {
        List<PostResponseDto> posts = createPostList();
        PostListResponseDto expectedResponse = createPostListResponse(posts, false, true, 3);

        when(postService.getPosts(SEARCH_QUERY, VALID_PAGE_NUMBER, VALID_PAGE_SIZE))
                .thenReturn(expectedResponse);

        ResponseEntity<PostListResponseDto> response = postController.getPosts(
                SEARCH_QUERY, VALID_PAGE_NUMBER, VALID_PAGE_SIZE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(postService).getPosts(SEARCH_QUERY, VALID_PAGE_NUMBER, VALID_PAGE_SIZE);
    }

    @Test
    @DisplayName("Должен вернуть пустой список когда посты не найдены")
    void shouldReturnEmptyListWhenNoPostsFoundTest() {
        PostListResponseDto expectedResponse = createPostListResponse(List.of(), false, false, 0);

        when(postService.getPosts(SEARCH_QUERY, VALID_PAGE_NUMBER, VALID_PAGE_SIZE))
                .thenReturn(expectedResponse);

        ResponseEntity<PostListResponseDto> response = postController.getPosts(
                SEARCH_QUERY, VALID_PAGE_NUMBER, VALID_PAGE_SIZE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().posts().isEmpty());
        assertFalse(response.getBody().hasNext());
        assertFalse(response.getBody().hasPrev());
        verify(postService).getPosts(SEARCH_QUERY, VALID_PAGE_NUMBER, VALID_PAGE_SIZE);
    }

    @DisplayName("Должен корректно обработать различные параметры пагинации")
    @ParameterizedTest
    @CsvSource({
            "search1, 1, 5",
            "query, 2, 10",
            "test, 10, 20",
            "' ', 1, 1"
    })
    void shouldHandleDifferentPaginationParametersTest(String search, int pageNumber, int pageSize) {
        PostListResponseDto expectedResponse = createPostListResponse(createPostList(), true, false, 5);

        when(postService.getPosts(search, pageNumber, pageSize)).thenReturn(expectedResponse);

        ResponseEntity<PostListResponseDto> response = postController.getPosts(search, pageNumber, pageSize);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(postService).getPosts(search, pageNumber, pageSize);
    }

}
