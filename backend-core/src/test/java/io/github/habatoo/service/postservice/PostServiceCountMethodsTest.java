package io.github.habatoo.service.postservice;

import io.github.habatoo.service.impl.PostServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты методов incrementLikes, incrementCommentsCount и decrementCommentsCount класса PostServiceImpl.
 *
 * <p>
 * Охватывает все основные сценарии работы методов подсчёта лайков и комментариев:
 * <ul>
 *   <li>Успешное увеличение количества лайков для существующего поста</li>
 *   <li>Обработка ошибки при попытке увеличить лайки для несуществующего поста</li>
 *   <li>Корректная работа методов инкремента/декремента комментариев для поста с обновлением значения</li>
 *   <li>Корректная работа методов, если пост отсутствует (ветка if (post == null))</li>
 *   <li>Обработка исключения и выброс IllegalStateException при ошибке хранения или обновления комментариев</li>
 * </ul>
 * </p>
 */
@DisplayName("Тесты методов подсчёта лайков и комментариев")
class PostServiceCountMethodsTest extends PostServiceTestBase {

    /**
     * Проверяет, что метод incrementLikes успешно увеличивает количество лайков для поста.
     */
    @Test
    @DisplayName("Должен увеличить лайки успешно")
    void shouldIncrementLikesTest() {
        doNothing().when(postRepository).incrementLikes(VALID_POST_ID);
        when(postRepository.getPostById(VALID_POST_ID)).thenReturn(Optional.of(POST_RESPONSE_1_LIKES));

        int newLikes = postService.incrementLikes(VALID_POST_ID);

        assertEquals(POST_RESPONSE_1.likesCount() + 1, newLikes);
        verify(postRepository).incrementLikes(VALID_POST_ID);
    }

    /**
     * Проверяет, что при попытке увеличить лайки для несуществующего поста выбрасывается IllegalStateException.
     */
    @Test
    @DisplayName("Должен бросать исключение при увеличении лайков несуществующего поста")
    void shouldThrowWhenIncrementLikesNonexistentTest() {
        doNothing().when(postRepository).incrementLikes(INVALID_POST_ID);

        assertThrows(IllegalStateException.class, () -> postService.incrementLikes(INVALID_POST_ID));
    }

    /**
     * Проверяет корректную работу методов инкремента и декремента количества комментариев для существующего поста.
     * Диапазон охвата — оба метода подряд.
     */
    @ParameterizedTest(name = "Метод: {0}")
    @ValueSource(strings = {"incrementCommentsCount", "decrementCommentsCount"})
    @DisplayName("Должен корректно работать методы инкремента и декремента комментариев")
    void shouldHandleCommentsCountChangesTest(String methodName) {
        if ("incrementCommentsCount".equals(methodName)) {
            postService.incrementCommentsCount(VALID_POST_ID);
            verify(postRepository).incrementCommentsCount(VALID_POST_ID);
        } else {
            postService.decrementCommentsCount(VALID_POST_ID);
            verify(postRepository).decrementCommentsCount(VALID_POST_ID);
        }
    }

    /**
     * Проверяет, что decrementCommentsCount корректно работает при отсутствии поста (if (post == null)).
     * Убеждается, что обновление и запись не происходят.
     */
    @Test
    @DisplayName("decrementCommentsCount: ветка if (post == null) — ничего не обновляется")
    void decrementCommentsCountIfCacheMissTest() {
        postService = new PostServiceImpl(postRepository, fileStorageService);

        Long postId = 3L;
        doNothing().when(postRepository).decrementCommentsCount(postId);

        assertDoesNotThrow(() -> postService.decrementCommentsCount(postId));
        verify(postRepository, times(1)).decrementCommentsCount(postId);
    }

    /**
     * Проверяет, что incrementCommentsCount корректно работает при отсутствии поста (if (post == null)).
     * Убеждается, что обновление и запись не происходят.
     */
    @Test
    @DisplayName("incrementCommentsCount: ветка if (post == null) — ничего не обновляется")
    void incrementCommentsCountIfCacheMissTest() {
        postService = new PostServiceImpl(postRepository, fileStorageService);
        Long postId = 1L;
        doNothing().when(postRepository).incrementCommentsCount(postId);

        assertDoesNotThrow(() -> postService.incrementCommentsCount(postId));
        verify(postRepository, times(1)).incrementCommentsCount(postId);
    }

    /**
     * Проверяет выброс IllegalStateException при ошибке инкремента количества комментариев (catch).
     */
    @Test
    @DisplayName("incrementCommentsCount: ветка catch — выбрасывается IllegalStateException")
    void incrementCommentsCountThrowsExceptionTest() {
        Long postId = 2L;
        doThrow(new RuntimeException("db error")).when(postRepository).incrementCommentsCount(postId);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                postService.incrementCommentsCount(postId)
        );
        assertTrue(ex.getMessage().contains("Ошибка при увеличении комментариев для поста id " + postId));
        assertTrue(ex.getCause().getMessage().contains("db error"));
    }

    /**
     * Проверяет выброс IllegalStateException при ошибке декремента количества комментариев (catch).
     */
    @Test
    @DisplayName("decrementCommentsCount: ветка catch — выбрасывается IllegalStateException")
    void decrementCommentsCountThrowsExceptionTest() {
        Long postId = 4L;
        doThrow(new RuntimeException("db error")).when(postRepository).decrementCommentsCount(postId);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                postService.decrementCommentsCount(postId)
        );
        assertTrue(ex.getMessage().contains("Ошибка при уменьшении комментариев для поста id " + postId));
        assertTrue(ex.getCause().getMessage().contains("db error"));
    }
}
