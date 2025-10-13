package io.github.habatoo.repositories.sql;

import lombok.experimental.UtilityClass;

/**
 * Класс с SQL запросами для работы с постами.
 * Содержит все SQL выражения, используемые в PostRepository.
 */
@UtilityClass
public final class PostSqlQueries {

    public static final String FIND_ALL_POSTS = """
            SELECT p.id, p.title, p.text, p.likes_count, p.comments_count
            FROM post p
            ORDER BY p.created_at DESC
            """;

    public static final String CREATE_POST = """
            INSERT INTO post (title, text, likes_count, comments_count, created_at, updated_at)
            VALUES (?, ?, 0, 0, ?, ?)
            """;

    public static final String INSERT_INTO_TAG = """
            MERGE INTO tag (name, created_at)
            KEY(name)
            VALUES (?, CURRENT_TIMESTAMP)
            """;

    public static final String INSERT_INTO_POST_TAG = """
            MERGE INTO post_tag (post_id, tag_id, created_at)
            KEY (post_id, tag_id)
            VALUES (?, (SELECT id FROM tag WHERE name = ?), CURRENT_TIMESTAMP)
            """;

    public static final String UPDATE_POST = """
            UPDATE post
            SET title = ?, text = ?, updated_at = ?
            WHERE id = ?
            """;

    public static final String DELETE_POST = """
            DELETE FROM post WHERE id = ?
            """;

    public static final String GET_TAGS_FOR_POST = """
            SELECT t.name FROM tag t
            JOIN post_tag pt ON t.id = pt.tag_id
            WHERE pt.post_id = ?
            """;

    public static final String INCREMENT_LIKES = """
            UPDATE post SET likes_count = likes_count + 1 WHERE id = ?
            """;

    public static final String INCREMENT_COMMENTS_COUNT = """
            UPDATE post SET comments_count = comments_count + 1 WHERE id = ?
            """;

    public static final String DECREMENT_COMMENTS_COUNT = """
            UPDATE post
            SET comments_count = CASE WHEN comments_count > 0 THEN comments_count - 1 ELSE 0 END
            WHERE id = ?
            """;

    public static final String SELECT_POST_BY_ID = """
            SELECT id, title, text, likes_count, comments_count
            FROM post
            WHERE id = ?
            """;
}
