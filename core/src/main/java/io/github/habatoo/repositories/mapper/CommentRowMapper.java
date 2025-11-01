package io.github.habatoo.repositories.mapper;

import io.github.habatoo.dto.response.CommentResponseDto;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Маппер отаета из БД в DTO ответа.
 *
 * @see CommentResponseDto
 */
@Component
public class CommentRowMapper implements RowMapper<CommentResponseDto> {
    @Override
    public CommentResponseDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new CommentResponseDto(
                rs.getLong("id"),
                rs.getString("text"),
                rs.getLong("post_id")
        );
    }
}
