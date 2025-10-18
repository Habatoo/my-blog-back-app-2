package io.github.habatoo.service.impl;

import io.github.habatoo.repositories.ImageRepository;
import io.github.habatoo.service.FileStorageService;
import io.github.habatoo.service.ImageContentTypeDetector;
import io.github.habatoo.service.ImageService;
import io.github.habatoo.service.ImageValidator;
import io.github.habatoo.service.dto.ImageResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Map;
import java.util.Optional;
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

    private static final String SEPARATOR = FileSystems.getDefault().getSeparator();

    /**
     * Получить URL для изображения поста.
     * @param postId идентификатор поста
     * @param newFileName имя нового файла изображения
     * @return строка вида "postId{separator}newFileName"
     */
    private static String buildImageUrl(Long postId, String newFileName) {
        return String.format("%s%s%s", postId, SEPARATOR, newFileName);
    }

    private final ImageRepository imageRepository;
    private final FileStorageService fileStorageService;
    private final ImageValidator imageValidator;
    private final ImageContentTypeDetector contentTypeDetector;

    private final Map<Long, ImageResponseDto> imageCache = new ConcurrentHashMap<>();

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

            String url = buildImageUrl(postId, newFileName);
            long size = image.getSize();
            imageRepository.updateImageMetadata(postId, newFileName, size, url);

            if (oldFileName != null) {
                log.info("Удаление старого изображения '{}' для поста id={}", oldFileName, postId);
                fileStorageService.deleteImageFile(oldFileName);
            }

            byte[] imageData = fileStorageService.loadImageFile(url);
            MediaType mediaType = contentTypeDetector.detect(imageData);

            imageCache.put(postId, new ImageResponseDto(imageData, mediaType));
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
    public ImageResponseDto getPostImage(Long postId) {
        log.debug("Запрос на получение изображения для поста id={}", postId);
        imageValidator.validatePostId(postId);

        if (!imageRepository.existsPostById(postId)) {
            log.warn("Пост id={} не найден при запросе изображения", postId);
            throw new IllegalStateException("Post not found with id: " + postId);
        }

        ImageResponseDto cached = imageCache.get(postId);
        if (cached != null) {
            log.debug("Изображение для поста id={} получено из кэша", postId);
            return cached;
        }

        Optional<String> fileName = imageRepository.findImageFileNameByPostId(postId);
        byte[] imageData;
        MediaType mediaType;

        try {
            if (fileName.isEmpty()) {
                imageData = new byte[]{};
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            } else {
                String url = buildImageUrl(postId, fileName.get());
                imageData = fileStorageService.loadImageFile(url);
                mediaType = contentTypeDetector.detect(imageData);
            }

            ImageResponseDto imageResponse = new ImageResponseDto(imageData, mediaType);
            imageCache.put(postId, imageResponse);
            log.info("Изображение для поста id={} загружено и кэшировано", postId);

            return imageResponse;
        } catch (IOException e) {
            log.error("Ошибка при загрузке изображения '{}' для поста id={}: {}", fileName, postId, e.getMessage(), e);
            throw new IllegalStateException("Failed to load image", e);
        }
    }
}
