package io.github.habatoo.service.postservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

/**
 * Тесты метода deletePost класса PostServiceImpl
 */
@DisplayName("Тесты метода deletePost")
class PostServiceDeletePostTest extends PostServiceTestBase {

    @Test
    @DisplayName("Должен удалить пост, обновить кеш и удалить директорию")
    void shouldDeletePostAndCleanupTest() {
        doNothing().when(postRepository).deletePost(VALID_POST_ID);
        doNothing().when(fileStorageService).deletePostDirectory(VALID_POST_ID);

        postService.deletePost(VALID_POST_ID);

        assertTrue(postService.getPostById(VALID_POST_ID).isEmpty());
        verify(postRepository).deletePost(VALID_POST_ID);
        verify(fileStorageService).deletePostDirectory(VALID_POST_ID);
    }
}
