package io.github.habatoo.repositories.mapper;

import io.github.habatoo.dto.response.CommentResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Маппер отаета из БД в DTO ответа.
 *
 * @see CommentResponse
 */
@Component
public class CommentRowMapper implements RowMapper<CommentResponse> {
    @Override
    public CommentResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new CommentResponse(
                rs.getLong("id"),
                rs.getString("text"),
                rs.getLong("post_id")
        );
    }
}
