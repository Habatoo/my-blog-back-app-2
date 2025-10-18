package io.github.habatoo.service.impl;

import io.github.habatoo.repositories.ImageRepository;
import io.github.habatoo.service.FileStorageService;
import io.github.habatoo.service.ImageContentTypeDetector;
import io.github.habatoo.service.ImageService;
import io.github.habatoo.service.ImageValidator;
import io.github.habatoo.service.dto.ImageResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
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
        validateUpdate(postId, image);

        String oldFileName = getOldFileName(postId);

        try {
            String newFileName = storeImageFile(postId, image);
            updateImageMetadata(postId, newFileName, image.getSize());
            deleteOldFileIfExists(postId, oldFileName);
            cacheImage(postId, newFileName);
        } catch (IOException e) {
            handleProcessingError(postId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImageResponseDto getPostImage(Long postId) {
        log.debug("Запрос на получение изображения для поста id={}", postId);
        validateGet(postId);

        ImageResponseDto cached = imageCache.get(postId);
        if (cached != null) {
            log.debug("Изображение для поста id={} получено из кэша", postId);
            return cached;
        }
        return loadAndCacheImage(postId);
    }

    private void validateUpdate(Long postId, MultipartFile image) {
        imageValidator.validateImageUpdate(postId, image);
        validateGet(postId);
    }

    private void validateGet(Long postId) {
        imageValidator.validatePostId(postId);
        if (!imageRepository.existsPostById(postId)) {
            log.warn("Пост id={} не найден при запросе изображения", postId);
            throw new EmptyResultDataAccessException("Post not found with id: " + postId, 1);
        }
    }

    private String getOldFileName(Long postId) {
        return imageRepository.findImageFileNameByPostId(postId).orElse(null);
    }

    private String storeImageFile(Long postId, MultipartFile image) throws IOException {
        String newFileName = fileStorageService.saveImageFile(postId, image);
        log.info("Файл изображения '{}' сохранён для поста id={}", newFileName, postId);
        return newFileName;
    }

    private void updateImageMetadata(Long postId, String newFileName, long size) {
        String url = buildImageUrl(postId, newFileName);
        imageRepository.updateImageMetadata(postId, newFileName, size, url);
    }

    private void deleteOldFileIfExists(Long postId, String oldFileName) throws IOException {
        if (oldFileName != null) {
            log.info("Удаление старого изображения '{}' для поста id={}", oldFileName, postId);
            fileStorageService.deleteImageFile(oldFileName);
        }
    }

    private void cacheImage(Long postId, String fileName) throws IOException {
        String url = buildImageUrl(postId, fileName);
        byte[] imageData = fileStorageService.loadImageFile(url);
        MediaType mediaType = contentTypeDetector.detect(imageData);
        imageCache.put(postId, new ImageResponseDto(imageData, mediaType));
        log.info("Кэшировано новое изображение для поста id={}", postId);
    }

    private void handleProcessingError(Long postId, IOException e) {
        log.error("Ошибка при обработке изображения для поста id={}: {}", postId, e.getMessage(), e);
        throw new IllegalStateException("Failed to process image file", e);
    }

    private ImageResponseDto loadAndCacheImage(Long postId) {
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
