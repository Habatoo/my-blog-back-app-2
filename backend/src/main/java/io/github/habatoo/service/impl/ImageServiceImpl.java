package io.github.habatoo.service.impl;

import io.github.habatoo.repository.ImageRepository;
import io.github.habatoo.service.FileStorageService;
import io.github.habatoo.service.ImageContentTypeDetector;
import io.github.habatoo.service.ImageService;
import io.github.habatoo.service.ImageValidator;
import io.github.habatoo.service.dto.ImageResponse;
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
        imageValidator.validatePostId(postId);
        imageValidator.validateImageUpdate(postId, image);

        if (!imageRepository.existsPostById(postId)) {
            throw new IllegalStateException("Post not found with id: " + postId);
        }

        String oldFileName = imageRepository.findImageFileNameByPostId(postId).orElse(null);

        try {
            String newFileName = fileStorageService.saveImageFile(postId, image);
            imageRepository.updateImageMetadata(postId, newFileName, image.getOriginalFilename(), image.getSize());

            if (oldFileName != null) {
                fileStorageService.deleteImageFile(oldFileName);
            }

            byte[] imageData = fileStorageService.loadImageFile(newFileName);
            MediaType mediaType = contentTypeDetector.detect(imageData);

            imageCache.put(postId, new ImageResponse(imageData, mediaType));

        } catch (IOException e) {
            throw new IllegalStateException("Failed to process image file", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImageResponse getPostImage(Long postId) {
        imageValidator.validatePostId(postId);

        if (!imageRepository.existsPostById(postId)) {
            throw new IllegalStateException("Post not found with id: " + postId);
        }

        ImageResponse cached = imageCache.get(postId);
        if (cached != null) {
            return cached;
        }

        String fileName = imageRepository.findImageFileNameByPostId(postId)
                .orElseThrow(() -> new IllegalStateException("Image not found for post id: " + postId));

        try {
            byte[] imageData = fileStorageService.loadImageFile(fileName);
            MediaType mediaType = contentTypeDetector.detect(imageData);

            ImageResponse imageResponse = new ImageResponse(imageData, mediaType);
            imageCache.put(postId, imageResponse);
            return imageResponse;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load image", e);
        }

    }

}
