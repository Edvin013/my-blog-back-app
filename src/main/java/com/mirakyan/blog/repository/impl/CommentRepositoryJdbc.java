package com.mirakyan.blog.repository.impl;

import com.mirakyan.blog.model.Comment;
import com.mirakyan.blog.repository.CommentRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CommentRepositoryJdbc implements CommentRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public CommentRepositoryJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Comment> mapper = new RowMapper<>() {
        @Override
        public Comment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Comment.CommentBuilder b = Comment.builder();
            b.id(rs.getLong("id"));
            b.text(rs.getString("text"));
            b.postId(rs.getLong("post_id"));
            Timestamp c = rs.getTimestamp("created_at");
            Timestamp u = rs.getTimestamp("updated_at");
            b.createdAt(c == null ? null : c.toInstant());
            b.updatedAt(u == null ? null : u.toInstant());
            return b.build();
        }
    };

    @Override
    public List<Comment> findByPostId(Long postId) {
        String sql = "SELECT id, text, post_id, created_at, updated_at FROM comments WHERE post_id=:postId ORDER BY created_at ASC";
        return jdbc.query(sql, Map.of("postId", postId), mapper);
    }

    @Override
    public Optional<Comment> findById(Long id) {
        String sql = "SELECT id, text, post_id, created_at, updated_at FROM comments WHERE id=:id";
        List<Comment> list = jdbc.query(sql, Map.of("id", id), mapper);
        return list.stream().findFirst();
    }

    @Override
    public Comment save(Comment comment) {
        if (comment.getId() == null) {
            return insert(comment);
        } else {
            return update(comment);
        }
    }

    private Comment insert(Comment comment) {
        String sql = "INSERT INTO comments (text, post_id, created_at, updated_at) VALUES (:text, :post_id, :created_at, :updated_at)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("text", comment.getText());
        params.addValue("post_id", comment.getPostId());
        params.addValue("created_at", Timestamp.from(comment.getCreatedAt() == null ? Instant.now() : comment.getCreatedAt()));
        params.addValue("updated_at", Timestamp.from(comment.getUpdatedAt() == null ? Instant.now() : comment.getUpdatedAt()));
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(sql, params, kh, new String[]{"id"});
        if (kh.getKey() != null) {
            comment.setId(kh.getKey().longValue());
        }
        return comment;
    }

    private Comment update(Comment comment) {
        String sql = "UPDATE comments SET text=:text, updated_at=:updated_at WHERE id=:id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", comment.getId());
        params.addValue("text", comment.getText());
        params.addValue("updated_at", Timestamp.from(comment.getUpdatedAt() == null ? Instant.now() : comment.getUpdatedAt()));
        jdbc.update(sql, params);
        return comment;
    }

    @Override
    public void deleteById(Long id) {
        jdbc.update("DELETE FROM comments WHERE id=:id", Map.of("id", id));
    }
}
