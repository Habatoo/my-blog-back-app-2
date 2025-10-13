package io.github.habatoo.service.imageservice;

import io.github.habatoo.service.dto.ImageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты метода getPostImage класса ImageServiceImpl.
 *
 * <p>
 * Охват тестов:
 * <ul>
 *   <li>Проверка кеширования изображений после updatePostImage и возврата их из кеша</li>
 *   <li>Загрузка и кеширование изображений при их отсутствии в кеше</li>
 *   <li>Обработка ошибок: отсутствие поста, отсутствие изображения, ошибка загрузки файла</li>
 *   <li>Ветвление обновления изображения: случай отсутствия старого файла</li>
 * </ul>
 * </p>
 */
@DisplayName("Тесты метода getPostImage")
class ImageServiceGetPostImageTest extends ImageServiceTestBase {

    /**
     * Проверяет, что изображение сохраняется через updatePostImage, кэшируется,
     * и оба публичных вызова getPostImage возвращают одно и то же (то же по ссылке) изображение из кеша.
     */
    @Test
    @DisplayName("Должен сохранить изображение через updatePostImage и вернуть его через getPostImage (проверка кэша по публичным методам)")
    void shouldCacheImageAfterUpdateAndReturnCachedImageTest() throws IOException {
        MultipartFile imageFile = createMultipartFile(false, ORIGINAL_FILENAME, IMAGE_SIZE);

        doNothing().when(imageValidator).validatePostId(VALID_POST_ID);
        doNothing().when(imageValidator).validateImageUpdate(VALID_POST_ID, imageFile);
        when(imageRepository.existsPostById(VALID_POST_ID)).thenReturn(true);
        when(imageRepository.findImageFileNameByPostId(VALID_POST_ID)).thenReturn(Optional.of(IMAGE_FILENAME));
        when(fileStorageService.saveImageFile(VALID_POST_ID, imageFile)).thenReturn(IMAGE_FILENAME);
        doNothing().when(imageRepository).updateImageMetadata(VALID_POST_ID, IMAGE_FILENAME, ORIGINAL_FILENAME, IMAGE_SIZE);
        doNothing().when(fileStorageService).deleteImageFile(IMAGE_FILENAME);
        when(fileStorageService.loadImageFile(IMAGE_FILENAME)).thenReturn(IMAGE_DATA);
        when(contentTypeDetector.detect(IMAGE_DATA)).thenReturn(MEDIA_TYPE);

        imageService.updatePostImage(VALID_POST_ID, imageFile);

        ImageResponse firstCall = imageService.getPostImage(VALID_POST_ID);
        ImageResponse secondCall = imageService.getPostImage(VALID_POST_ID);

        assertArrayEquals(IMAGE_DATA, firstCall.data());
        assertEquals(MEDIA_TYPE, firstCall.mediaType());

        assertSame(firstCall, secondCall, "Изображения должны быть получены из кеша и совпадать по ссылке");

        verify(fileStorageService, times(1)).saveImageFile(VALID_POST_ID, imageFile);
        verify(fileStorageService, times(1)).loadImageFile(IMAGE_FILENAME);
        verify(imageRepository).updateImageMetadata(VALID_POST_ID, IMAGE_FILENAME, ORIGINAL_FILENAME, IMAGE_SIZE);
        verify(fileStorageService).deleteImageFile(IMAGE_FILENAME);
    }

    /**
     * Проверяет, что при отсутствии изображения в кеше getPostImage загружает его с диска,
     * определяет mediaType и кладёт в кеш для последующих вызовов.
     */
    @Test
    @DisplayName("Должен загрузить изображение из файловой системы и кэшировать его при отсутствии в кэше")
    void shouldLoadImageFromFileAndCacheTest() throws IOException {
        ImageResponse expectedResponse = new ImageResponse(IMAGE_DATA, MEDIA_TYPE);

        doNothing().when(imageValidator).validatePostId(VALID_POST_ID);
        when(imageRepository.existsPostById(VALID_POST_ID)).thenReturn(true);
        when(imageRepository.findImageFileNameByPostId(VALID_POST_ID)).thenReturn(Optional.of(IMAGE_FILENAME));
        when(fileStorageService.loadImageFile(IMAGE_FILENAME)).thenReturn(IMAGE_DATA);
        when(contentTypeDetector.detect(IMAGE_DATA)).thenReturn(MEDIA_TYPE);

        ImageResponse result = imageService.getPostImage(VALID_POST_ID);

        assertEquals(expectedResponse.mediaType(), result.mediaType());
        assertArrayEquals(expectedResponse.data(), result.data());

        verify(imageValidator).validatePostId(VALID_POST_ID);
        verify(imageRepository).existsPostById(VALID_POST_ID);
        verify(imageRepository).findImageFileNameByPostId(VALID_POST_ID);
        verify(fileStorageService).loadImageFile(IMAGE_FILENAME);
        verify(contentTypeDetector).detect(IMAGE_DATA);
    }

    /**
     * Проверяет, что при отсутствии поста вызывается и выбрасывается IllegalStateException с корректным сообщением.
     */
    @Test
    @DisplayName("Должен выбросить исключение при отсутствии поста")
    void shouldThrowIfPostNotFoundTest() {
        doNothing().when(imageValidator).validatePostId(INVALID_POST_ID);
        when(imageRepository.existsPostById(INVALID_POST_ID)).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> imageService.getPostImage(INVALID_POST_ID));
        assertTrue(ex.getMessage().contains("Post not found with id"));

        verify(imageValidator).validatePostId(INVALID_POST_ID);
        verify(imageRepository).existsPostById(INVALID_POST_ID);
        verifyNoInteractions(fileStorageService);
    }

    /**
     * Проверяет, что при отсутствии изображения для существующего поста выбрасывается IllegalStateException.
     */
    @Test
    @DisplayName("Должен выбросить исключение при отсутствии изображения для поста")
    void shouldThrowIfImageNotFoundTest() {
        doNothing().when(imageValidator).validatePostId(VALID_POST_ID);
        when(imageRepository.existsPostById(VALID_POST_ID)).thenReturn(true);
        when(imageRepository.findImageFileNameByPostId(VALID_POST_ID)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> imageService.getPostImage(VALID_POST_ID));
        assertTrue(ex.getMessage().contains("Image not found for post id"));

        verify(imageValidator).validatePostId(VALID_POST_ID);
        verify(imageRepository).existsPostById(VALID_POST_ID);
        verify(imageRepository).findImageFileNameByPostId(VALID_POST_ID);
        verifyNoInteractions(fileStorageService);
    }

    /**
     * Проверяет, что при ошибке чтения файла (например, IOException) выбрасывается IllegalStateException с корректным сообщением.
     */
    @Test
    @DisplayName("Должен выбросить исключение при ошибке загрузки файла")
    void shouldThrowWhenIOExceptionDuringLoadTest() throws IOException {
        doNothing().when(imageValidator).validatePostId(VALID_POST_ID);
        when(imageRepository.existsPostById(VALID_POST_ID)).thenReturn(true);
        when(imageRepository.findImageFileNameByPostId(VALID_POST_ID)).thenReturn(Optional.of(IMAGE_FILENAME));
        when(fileStorageService.loadImageFile(IMAGE_FILENAME)).thenThrow(new IOException("Load failed"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> imageService.getPostImage(VALID_POST_ID));
        assertTrue(ex.getMessage().contains("Failed to load image"));

        verify(imageValidator).validatePostId(VALID_POST_ID);
        verify(imageRepository).existsPostById(VALID_POST_ID);
        verify(imageRepository).findImageFileNameByPostId(VALID_POST_ID);
        verify(fileStorageService).loadImageFile(IMAGE_FILENAME);
    }

    /**
     * Проверяет, что если у поста не было старого файла, метод удаления файла не вызывается при updatePostImage.
     */
    @Test
    @DisplayName("Не должен вызывать удаление файла, если старого файла нет")
    void shouldNotCallDeleteIfOldFileIsNull() throws IOException {
        MultipartFile imageFile = createMultipartFile(false, ORIGINAL_FILENAME, IMAGE_SIZE);

        when(imageRepository.existsPostById(VALID_POST_ID)).thenReturn(true);
        when(imageRepository.findImageFileNameByPostId(VALID_POST_ID)).thenReturn(Optional.empty());
        when(fileStorageService.saveImageFile(eq(VALID_POST_ID), eq(imageFile))).thenReturn("newfile.jpg");
        when(imageFile.getOriginalFilename()).thenReturn("orig.jpg");
        when(imageFile.getSize()).thenReturn(123L);
        byte[] imageData = new byte[]{1, 2, 3};
        when(fileStorageService.loadImageFile("newfile.jpg")).thenReturn(imageData);
        when(contentTypeDetector.detect(imageData)).thenReturn(MediaType.IMAGE_JPEG);

        imageService.updatePostImage(VALID_POST_ID, imageFile);

        verify(fileStorageService, never()).deleteImageFile(anyString());
        verify(fileStorageService).saveImageFile(VALID_POST_ID, imageFile);
        verify(imageRepository).updateImageMetadata(eq(VALID_POST_ID), eq("newfile.jpg"), eq("orig.jpg"), eq(123L));
    }
}

