package io.github.habatoo.repositories.sql;

import lombok.experimental.UtilityClass;

/**
 * SQL запросы для работы с комментариями.
 * Содержит все SQL выражения, используемые в CommentRepository.
 */
@UtilityClass
public final class CommentSqlQueries {

    public static final String FIND_BY_POST_ID = """
            SELECT id, text, post_id
            FROM comment
            WHERE post_id = ?
            ORDER BY created_at ASC
            """;

    public static final String FIND_BY_POST_ID_AND_ID = """
            SELECT id, text, post_id
            FROM comment
            WHERE post_id = ? AND id = ?
            """;

    public static final String FIND_BY_ID = """
            SELECT id, text, post_id
            FROM comment
            WHERE id = ?
            """;

    public static final String INSERT_COMMENT = """
            INSERT INTO comment (post_id, text, created_at, updated_at)
            VALUES (?, ?, ?, ?)
            """;

    public static final String UPDATE_COMMENT_TEXT = """
            UPDATE comment
            SET text = ?, updated_at = ?
            WHERE id = ?
            """;

    public static final String DELETE_COMMENT = """
            DELETE FROM comment WHERE id = ?
            """;
}
