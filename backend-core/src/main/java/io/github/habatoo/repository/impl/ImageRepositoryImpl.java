package io.github.habatoo.repository.impl;

import io.github.habatoo.repository.ImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static io.github.habatoo.repository.sql.ImageSqlQueries.*;

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
            String fileName = jdbcTemplate.queryForObject(GET_IMAGE_FILE_NAME, String.class, postId);

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
    public void updateImageMetadata(Long postId, String fileName, String originalName, long size) {
        log.info("Обновление метаданных изображения для поста id={} (fileName={}, originalName={}, size={})", postId, fileName, originalName, size);
        int updatedRows = jdbcTemplate.update(UPDATE_POST_IMAGE, originalName, size, fileName, postId);

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
        Integer count = jdbcTemplate.queryForObject(CHECK_POST_EXISTS, Integer.class, postId);

        return count != null && count > 0;
    }
}
