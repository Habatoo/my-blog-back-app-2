package io.github.habatoo.service.impl;

import io.github.habatoo.repository.ImageRepository;
import io.github.habatoo.service.FileStorageService;
import io.github.habatoo.service.ImageContentTypeDetector;
import io.github.habatoo.service.ImageService;
import io.github.habatoo.service.ImageValidator;
import io.github.habatoo.service.dto.ImageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис для обработки изображений поста.
 *
 * @see ImageRepository
 * @see FileStorageService
 * @see ImageValidator
 * @see ImageContentTypeDetector
 */
@Slf4j
@Service
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final FileStorageService fileStorageService;
    private final ImageValidator imageValidator;
    private final ImageContentTypeDetector contentTypeDetector;

    private final Map<Long, ImageResponse> imageCache = new ConcurrentHashMap<>();

    public ImageServiceImpl(
            ImageRepository imageRepository,
            FileStorageService fileStorageService,
            ImageValidator imageValidator,
            ImageContentTypeDetector contentTypeDetector) {
        this.imageRepository = imageRepository;
        this.fileStorageService = fileStorageService;
        this.imageValidator = imageValidator;
        this.contentTypeDetector = contentTypeDetector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePostImage(Long postId, MultipartFile image) {
        log.info("Запрос обновления изображения для поста id={}, оригинальное имя '{}', размер {} байт",
                postId, image.getOriginalFilename(), image.getSize());
        imageValidator.validatePostId(postId);
        imageValidator.validateImageUpdate(postId, image);

        if (!imageRepository.existsPostById(postId)) {
            log.warn("Пост id={} не найден при обновлении изображения", postId);
            throw new IllegalStateException("Post not found with id: " + postId);
        }

        String oldFileName = imageRepository.findImageFileNameByPostId(postId).orElse(null);

        try {
            String newFileName = fileStorageService.saveImageFile(postId, image);
            log.info("Файл изображения '{}' сохранён для поста id={}", newFileName, postId);
            imageRepository.updateImageMetadata(postId, newFileName, image.getOriginalFilename(), image.getSize());

            if (oldFileName != null) {
                log.info("Удаление старого изображения '{}' для поста id={}", oldFileName, postId);
                fileStorageService.deleteImageFile(oldFileName);
            }

            byte[] imageData = fileStorageService.loadImageFile(newFileName);
            MediaType mediaType = contentTypeDetector.detect(imageData);

            imageCache.put(postId, new ImageResponse(imageData, mediaType));
            log.info("Кэшировано новое изображение для поста id={}", postId);

        } catch (IOException e) {
            log.error("Ошибка при обработке изображения для поста id={}: {}", postId, e.getMessage(), e);
            throw new IllegalStateException("Failed to process image file", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImageResponse getPostImage(Long postId) {
        log.debug("Запрос на получение изображения для поста id={}", postId);
        imageValidator.validatePostId(postId);

        if (!imageRepository.existsPostById(postId)) {
            log.warn("Пост id={} не найден при запросе изображения", postId);
            throw new IllegalStateException("Post not found with id: " + postId);
        }

        ImageResponse cached = imageCache.get(postId);
        if (cached != null) {
            log.debug("Изображение для поста id={} получено из кэша", postId);
            return cached;
        }

        String fileName = imageRepository.findImageFileNameByPostId(postId)
                .orElseThrow(() -> {
                    log.warn("Изображение для поста id={} не найдено в БД", postId);
                    return new IllegalStateException("Image not found for post id: " + postId);
                });

        try {
            byte[] imageData = fileStorageService.loadImageFile(fileName);
            MediaType mediaType = contentTypeDetector.detect(imageData);

            ImageResponse imageResponse = new ImageResponse(imageData, mediaType);
            imageCache.put(postId, imageResponse);
            log.info("Изображение для поста id={} загружено и кэшировано", postId);
            return imageResponse;
        } catch (IOException e) {
            log.error("Ошибка при загрузке изображения '{}' для поста id={}: {}", fileName, postId, e.getMessage(), e);
            throw new IllegalStateException("Failed to load image", e);
        }
    }
}
