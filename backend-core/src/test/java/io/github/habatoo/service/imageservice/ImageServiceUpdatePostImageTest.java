package io.github.habatoo.service.imageservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Тесты метода updatePostImage класса ImageServiceImpl.
 *
 * <p>
 * Охватываемые сценарии:
 * <ul>
 *   <li>Успешное обновление изображения поста с удалением предыдущего и обновлением метаданных</li>
 *   <li>Выброс исключения, если пост не найден (нет postId в репозитории)</li>
 *   <li>Обработка ошибок ввода-вывода и выброс исключения при проблемах с файловой системой</li>
 * </ul>
 * Каждый тест проверяет публичное API и ключевые ветви метода updatePostImage.
 * </p>
 */
@DisplayName("Тесты метода updatePostImage")
class ImageServiceUpdatePostImageTest extends ImageServiceTestBase {

    /**
     * Проверяет успешное обновление изображения:
     * валидация, сохранение нового файла, удаление старого, обновление метаданных
     * и корректное определение mediaType.
     */
    @Test
    @DisplayName("Должен корректно обновить изображение поста при валидных данных")
    void shouldUpdatePostImageSuccessfullyTest() throws IOException {
        MultipartFile imageFile = createMultipartFile(false, ORIGINAL_FILENAME, IMAGE_SIZE);

        doNothing().when(imageValidator).validatePostId(VALID_POST_ID);
        doNothing().when(imageValidator).validateImageUpdate(VALID_POST_ID, imageFile);
        when(imageRepository.existsPostById(VALID_POST_ID)).thenReturn(true);
        when(imageRepository.findImageFileNameByPostId(VALID_POST_ID)).thenReturn(Optional.of(IMAGE_FILENAME));
        when(fileStorageService.saveImageFile(VALID_POST_ID, imageFile)).thenReturn(IMAGE_FILENAME);
        doNothing().when(imageRepository).updateImageMetadata(VALID_POST_ID, IMAGE_FILENAME, IMAGE_SIZE, URL);
        doNothing().when(fileStorageService).deleteImageFile(IMAGE_FILENAME);

        imageService.updatePostImage(VALID_POST_ID, imageFile);

        verify(imageValidator).validatePostId(VALID_POST_ID);
        verify(imageValidator).validateImageUpdate(VALID_POST_ID, imageFile);
        verify(imageRepository).existsPostById(VALID_POST_ID);
        verify(imageRepository).findImageFileNameByPostId(VALID_POST_ID);
        verify(fileStorageService).saveImageFile(VALID_POST_ID, imageFile);
        verify(imageRepository).updateImageMetadata(VALID_POST_ID, IMAGE_FILENAME, IMAGE_SIZE, URL);
        verify(fileStorageService).deleteImageFile(IMAGE_FILENAME);
    }

    /**
     * Проверяет выброс EmptyResultDataAccessException, если в репозитории не найден пост с указанным id.
     */
    @Test
    @DisplayName("Должен выбросить исключение если пост не найден")
    void shouldThrowIfPostNotFoundTest() {
        MultipartFile imageFile = createMultipartFile(false, ORIGINAL_FILENAME, IMAGE_SIZE);

        doNothing().when(imageValidator).validatePostId(INVALID_POST_ID);
        doNothing().when(imageValidator).validateImageUpdate(INVALID_POST_ID, imageFile);
        when(imageRepository.existsPostById(INVALID_POST_ID)).thenReturn(false);

        EmptyResultDataAccessException ex = assertThrows(EmptyResultDataAccessException.class,
                () -> imageService.updatePostImage(INVALID_POST_ID, imageFile));

        assertTrue(ex.getMessage().contains("Post not found with id"));

        verify(imageValidator).validatePostId(INVALID_POST_ID);
        verify(imageValidator).validateImageUpdate(INVALID_POST_ID, imageFile);
        verify(imageRepository).existsPostById(INVALID_POST_ID);
        verifyNoMoreInteractions(fileStorageService);
    }

    /**
     * Проверяет выброс IllegalStateException при ошибке во время сохранения или загрузки файла изображения (например IOException).
     */
    @Test
    @DisplayName("Должен выбросить исключение если произошла ошибка при работе с файлом")
    void shouldThrowWhenIOExceptionOccursTest() throws IOException {
        MultipartFile imageFile = createMultipartFile(false, ORIGINAL_FILENAME, IMAGE_SIZE);

        doNothing().when(imageValidator).validatePostId(VALID_POST_ID);
        doNothing().when(imageValidator).validateImageUpdate(VALID_POST_ID, imageFile);
        when(imageRepository.existsPostById(VALID_POST_ID)).thenReturn(true);
        when(imageRepository.findImageFileNameByPostId(VALID_POST_ID)).thenReturn(Optional.of(IMAGE_FILENAME));
        when(fileStorageService.saveImageFile(VALID_POST_ID, imageFile)).thenThrow(new IOException("IO error"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> imageService.updatePostImage(VALID_POST_ID, imageFile));

        assertTrue(ex.getMessage().contains("Failed to process image file"));

        verify(imageValidator).validatePostId(VALID_POST_ID);
        verify(imageValidator).validateImageUpdate(VALID_POST_ID, imageFile);
        verify(imageRepository).existsPostById(VALID_POST_ID);
        verify(imageRepository).findImageFileNameByPostId(VALID_POST_ID);
        verify(fileStorageService).saveImageFile(VALID_POST_ID, imageFile);
    }
}
