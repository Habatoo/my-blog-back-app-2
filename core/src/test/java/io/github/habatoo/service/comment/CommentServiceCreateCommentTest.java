package io.github.habatoo.service.comment;

import io.github.habatoo.dto.request.CommentCreateRequestDto;
import io.github.habatoo.dto.response.CommentResponseDto;
import io.github.habatoo.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
     */
    @Test
    @DisplayName("Должен создать комментарий и счетчик комментариев поста")
    void shouldCreateCommentAndUpdateCacheAndPostTest() {
        CommentCreateRequestDto request = createCommentCreateRequest(COMMENT_TEXT, VALID_POST_ID);
        CommentResponseDto savedComment = createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, COMMENT_TEXT);

        when(commentRepository.save(request)).thenReturn(savedComment);

        CommentResponseDto result = commentService.createComment(request);

        assertEquals(savedComment, result);
        verify(commentRepository).save(request);
        verify(postService).incrementCommentsCount(VALID_POST_ID);
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
