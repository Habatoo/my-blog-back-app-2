package io.github.habatoo.repositories.impl;

import io.github.habatoo.repositories.ImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Реализация репозитория для работы с метаданными изображений постов.
 *
 * <p>Использует JdbcTemplate для выполнения SQL-запросов к базе данных.
 * Предоставляет доступ к метаданным изображений, хранящимся в таблице постов.</p>
 */
@Slf4j
@Repository
public class ImageRepositoryImpl implements ImageRepository {

    private final JdbcTemplate jdbcTemplate;

    public ImageRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> findImageFileNameByPostId(Long postId) {
        log.debug("Поиск имени файла изображения для поста id={}", postId);
        try {
            String fileName = jdbcTemplate.queryForObject(
                    """
                            SELECT image_name
                            FROM post
                            WHERE id = ?
                            """,
                    String.class,
                    postId
            );

            return Optional.ofNullable(fileName);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Изображение для поста id={} не найдено", postId);

            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateImageMetadata(Long postId, String fileName, long size, String url) {
        log.info("Обновление метаданных изображения для поста id={} (fileName={}, size={}, url={})", postId, fileName, size, url);
        int updatedRows = jdbcTemplate.update(
                """
                        UPDATE post
                        SET image_name = ?, image_size = ?, image_url = ?
                        WHERE id = ?
                        """,
                fileName,
                size,
                url,
                postId
        );

        if (updatedRows == 0) {
            log.warn("Пост с id={} не найден при обновлении изображения", postId);
            throw new EmptyResultDataAccessException("Post not found with id: " + postId, 1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsPostById(Long postId) {
        log.debug("Проверка существования поста id={}", postId);
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM post
                        WHERE id = ?
                        """,
                Integer.class,
                postId
        );

        return count != null && count > 0;
    }
}
