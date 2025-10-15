package io.github.habatoo.controllers;

import io.github.habatoo.handlers.GlobalExceptionHandler;
import io.github.habatoo.service.ImageService;
import io.github.habatoo.service.dto.ImageResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Контроллер для управления изображениями постов.
 *
 * <p>Предоставляет REST API для загрузки и получения изображений, связанных с постами.
 * Поддерживает операции обновления и получения изображений в формате multipart/form-data.</p>
 *
 * @see ImageService
 * @see GlobalExceptionHandler
 */
@Slf4j
@RestController
@RequestMapping("/api/posts")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Обновляет изображение для указанного поста.
     *
     * <p>Обрабатывает PUT запросы по пути {@code /api/posts/{postId}/image}
     * для обновления или установки изображения поста. Фронтенд отправляет файл
     * в формате multipart/form-data.</p>
     *
     * <p><strong>Тип контента:</strong> multipart/form-data</p>
     * <p><strong>Максимальный размер файла:</strong> определяется конфигурацией Spring</p>
     *
     * @param postId идентификатор поста, для которого обновляется изображение
     * @param image  файл изображения, загружаемый клиентом. Должен быть валидным изображением
     * @return ResponseEntity со статусом 200 OK при успешном обновлении
     * @throws EmptyResultDataAccessException если пост с указанным ID не найден
     * @throws IllegalArgumentException       если файл изображения невалиден или пуст
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    @PutMapping("/{postId}/image")
    public ResponseEntity<Void> updatePostImage(
            @PathVariable("postId") Long postId,
            @RequestParam("image") MultipartFile image) {
        log.info("Запрос на обновление изображения для поста id={}", postId);
        imageService.updatePostImage(postId, image);
        return ResponseEntity.ok().build();
    }

    /**
     * Получает изображение для указанного поста.
     *
     * <p>Обрабатывает GET запросы по пути {@code /api/posts/{postId}/image}
     * для получения изображения, связанного с постом. Возвращает массив байт
     * изображения с соответствующим Content-Type.</p>
     *
     * <p><strong>Тип ответа:</strong> image/* (зависит от формата сохраненного изображения)</p>
     *
     * @param postId идентификатор поста, для которого запрашивается изображение
     * @return ResponseEntity с массивом байт изображения и соответствующим Content-Type
     * @throws EmptyResultDataAccessException если пост с указанным ID не найден
     * @throws EmptyResultDataAccessException если изображение для поста не найдено
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    @GetMapping("/{postId}/image")
    public ResponseEntity<byte[]> getPostImage(@PathVariable("postId") Long postId) {
        log.info("Запрос на получение изображения для поста id={}", postId);
        ImageResponseDto imageResponse = imageService.getPostImage(postId);
        return ResponseEntity.ok()
                .contentType(imageResponse.mediaType())
                .body(imageResponse.data());
    }

}
