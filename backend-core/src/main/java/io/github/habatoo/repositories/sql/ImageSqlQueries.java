package io.github.habatoo.repositories.sql;

import lombok.experimental.UtilityClass;

/**
 * Класс с SQL запросами для работы с изображениями.
 * Содержит все SQL выражения, используемые в ImageRepository.
 */
@UtilityClass
public final class ImageSqlQueries {
    public static final String GET_IMAGE_FILE_NAME = """
            SELECT image_name
            FROM post
            WHERE id = ?
            """;

    public static final String UPDATE_POST_IMAGE = """
            UPDATE post
            SET image_name = ?, image_size = ?, image_url = ?
            WHERE id = ?
            """;

    public static final String CHECK_POST_EXISTS = """
            SELECT COUNT(1)
            FROM post
            WHERE id = ?
            """;
}
