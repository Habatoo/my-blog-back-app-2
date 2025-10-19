package io.github.habatoo.service.comment;

import io.github.habatoo.dto.request.CommentCreateRequestDto;
import io.github.habatoo.dto.response.CommentResponseDto;
import io.github.habatoo.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты метода createComment класса CommentServiceImpl.
 *
 * <p>
 * Тестируются все критически важные сценарии:
 * <ul>
 *   <li>Создание комментария для существующего поста с обновлением кеша и счётчика комментариев</li>
 *   <li>Добавление нового комментария к уже существующему в кеше списку комментариев (ветка comments != null)</li>
 *   <li>Обработка ситуации, когда пост не существует — выбрасывается IllegalStateException</li>
 * </ul>
 * </p>
 */
@DisplayName("Тесты метода createComment")
class CommentServiceCreateCommentTest extends CommentServiceTestBase {

    /**
     * Проверяет создание комментария для существующего поста:
     * - Комментарий сохраняется в репозитории
     * - Счётчик комментариев поста инкрементируется
     * - Новый комментарий появляется в кеше среди списка комментариев поста
     */
    @Test
    @DisplayName("Должен создать комментарий и обновить кеш и счетчик комментариев поста")
    void shouldCreateCommentAndUpdateCacheAndPostTest() {
        CommentCreateRequestDto request = createCommentCreateRequest(COMMENT_TEXT, VALID_POST_ID);
        CommentResponseDto savedComment = createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, COMMENT_TEXT);

        when(commentRepository.save(request)).thenReturn(savedComment);

        CommentResponseDto result = commentService.createComment(request);

        assertEquals(savedComment, result);
        verify(commentRepository).save(request);
        verify(postService).incrementCommentsCount(VALID_POST_ID);

        List<CommentResponseDto> cachedComments = commentService.getCommentsByPostId(VALID_POST_ID);
        assertTrue(cachedComments.contains(savedComment));
    }

    /**
     * Проверяет добавление комментария в существующий список в кеше:
     * - Если в кеше по postId уже есть список, новый комментарий добавляется в этот список (comments != null)
     * - Сохраняется, инкрементируется счётчик, список не заменяется, а расширяется
     */
    @Test
    @DisplayName("Должен добавить новый комментарий в существующий список комментариев в кеше")
    void shouldAddCommentToExistingCacheList() {
        CommentCreateRequestDto request = createCommentCreateRequest(COMMENT_TEXT, VALID_POST_ID);
        CommentResponseDto savedComment = createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, COMMENT_TEXT);

        when(commentRepository.save(request)).thenReturn(savedComment);
        commentService.createComment(request);

        CommentCreateRequestDto newRequest = createCommentCreateRequest(COMMENT_TEXT_NEW, VALID_POST_ID);
        CommentResponseDto newSavedComment = createCommentResponse(VALID_COMMENT_ID_2, VALID_POST_ID, COMMENT_TEXT_NEW);

        when(commentRepository.save(newRequest)).thenReturn(newSavedComment);
        CommentResponseDto result = commentService.createComment(newRequest);

        assertEquals(newSavedComment, result);
        verify(commentRepository).save(newRequest);
        verify(postService, times(2)).incrementCommentsCount(VALID_POST_ID);

        List<CommentResponseDto> cachedComments = commentService.getCommentsByPostId(VALID_POST_ID);
        assertTrue(cachedComments.contains(newSavedComment));
    }

    /**
     * Юнит-тест проверяет сценарий, когда при создании комментария происходит ошибка
     * в репозитории (например, сбой БД), и метод {@link CommentService#createComment} ожидаемо
     * выбрасывает IllegalStateException c корректным сообщением и оригинальной причиной.
     * <p>
     * Тест мокаует зависимость commentRepository так, что save бросает RuntimeException,
     * и убеждается, что в сервисе выполняется обработка через catch с генерацией IllegalStateException,
     * а также что сообщение содержит id поста, а причина исключения соответствует ожидаемой.
     */
    @Test
    @DisplayName("createComment — выбрасывается IllegalStateException при ошибке сохранения")
    void testCreateCommentThrowsOnRepositoryErrorTest() {
        CommentCreateRequestDto request = new CommentCreateRequestDto(VALID_POST_ID, "text");

        when(commentRepository.save(any())).thenThrow(new RuntimeException("fail save"));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> commentService.createComment(request));

        assertTrue(ex.getMessage().contains("Комментарий к посту id=1 не создан"));
        assertNotNull(ex.getCause());
        assertEquals("fail save", ex.getCause().getMessage());
    }
}
