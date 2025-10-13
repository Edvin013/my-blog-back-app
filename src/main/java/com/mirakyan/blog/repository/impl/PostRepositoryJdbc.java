package com.mirakyan.blog.repository.impl;

import com.mirakyan.blog.model.Post;
import com.mirakyan.blog.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Repository
public class PostRepositoryJdbc implements PostRepository {

    private static final Logger log = LoggerFactory.getLogger(PostRepositoryJdbc.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final boolean h2Mode;

    public PostRepositoryJdbc(NamedParameterJdbcTemplate jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.h2Mode = detectH2(dataSource);
        log.info("PostRepositoryJdbc initialized, h2Mode={}", h2Mode);
    }

    private boolean detectH2(DataSource ds) {
        try (Connection c = ds.getConnection()) {
            String product = c.getMetaData().getDatabaseProductName();
            return product != null && product.toLowerCase(Locale.ROOT).contains("h2");
        } catch (Exception e) {
            log.warn("Could not detect DB product name: {}", e.getMessage());
            return false;
        }
    }

    private final RowMapper<Post> mapper = (ResultSet rs, int rowNum) -> {
        Post.PostBuilder b = Post.builder();
        b.id(rs.getLong("id"));
        b.title(rs.getString("title"));
        b.text(rs.getString("text"));
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        b.createdAt(created == null ? null : created.toInstant());
        b.updatedAt(updated == null ? null : updated.toInstant());
        b.likesCount(rs.getObject("likes_count") == null ? 0 : rs.getInt("likes_count"));
        b.commentsCount(rs.getObject("comments_count") == null ? 0 : rs.getInt("comments_count"));
        b.imagePath(rs.getString("image_path"));
        try {
            java.sql.Array arr = rs.getArray("tags");
            if (arr != null) {
                b.tags((String[]) arr.getArray());
            }
        } catch (Exception ignored) {}
        return b.build();
    };

    @Override
    public Optional<Post> findById(Long id) {
        String sql = "SELECT id, title, text, created_at, updated_at, likes_count, comments_count, image_path, tags FROM posts WHERE id=:id";
        List<Post> list = jdbc.query(sql, Map.of("id", id), mapper);
        return list.stream().findFirst();
    }

    @Override
    public Post save(Post post) {
        if (post.getId() == null) {
            return insert(post);
        } else {
            return update(post);
        }
    }

    private Post insert(Post post) {
        String sql = "INSERT INTO posts (title, text, created_at, updated_at, likes_count, comments_count, image_path, tags) " +
                "VALUES (:title, :text, :created_at, :updated_at, :likes_count, :comments_count, :image_path, :tags)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("title", post.getTitle());
        params.addValue("text", post.getText());
        params.addValue("created_at", Timestamp.from(Optional.ofNullable(post.getCreatedAt()).orElse(Instant.now())));
        params.addValue("updated_at", Timestamp.from(Optional.ofNullable(post.getUpdatedAt()).orElse(Instant.now())));
        params.addValue("likes_count", Optional.ofNullable(post.getLikesCount()).orElse(0));
        params.addValue("comments_count", Optional.ofNullable(post.getCommentsCount()).orElse(0));
        params.addValue("image_path", post.getImagePath());
        params.addValue("tags", post.getTags());
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(sql, params, kh, new String[]{"id"});
        Number key = kh.getKey();
        if (key != null) {
            post.setId(key.longValue());
        }
        return post;
    }

    private Post update(Post post) {
        String sql = "UPDATE posts SET title=:title, text=:text, updated_at=:updated_at, likes_count=:likes_count, comments_count=:comments_count, image_path=:image_path, tags=:tags WHERE id=:id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", post.getId());
        params.addValue("title", post.getTitle());
        params.addValue("text", post.getText());
        params.addValue("updated_at", Timestamp.from(Optional.ofNullable(post.getUpdatedAt()).orElse(Instant.now())));
        params.addValue("likes_count", Optional.ofNullable(post.getLikesCount()).orElse(0));
        params.addValue("comments_count", Optional.ofNullable(post.getCommentsCount()).orElse(0));
        params.addValue("image_path", post.getImagePath());
        params.addValue("tags", post.getTags());
        jdbc.update(sql, params);
        return post;
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT 1 FROM posts WHERE id=:id";
        List<Integer> list = jdbc.query(sql, Map.of("id", id), (rs, rn) -> rs.getInt(1));
        return !list.isEmpty();
    }

    @Override
    public void deleteById(Long id) {
        jdbc.update("DELETE FROM posts WHERE id=:id", Map.of("id", id));
    }

    private void appendTagConditions(StringBuilder sql, List<String> requiredTags, MapSqlParameterSource params) {
        if (requiredTags == null) return;
        int i = 0;
        for (String tag : requiredTags) {
            String name = "tag" + i;
            if (h2Mode) {
                sql.append(" AND ARRAY_CONTAINS(tags, :").append(name).append(")");
            } else {
                sql.append(" AND :").append(name).append(" = ANY(tags)");
            }
            params.addValue(name, tag.toLowerCase(Locale.ROOT));
            i++;
        }
    }

    @Override
    public List<Post> findFiltered(String titleSubstring, List<String> requiredTags, int offset, int limit) {
        StringBuilder sql = new StringBuilder("SELECT id, title, text, created_at, updated_at, likes_count, comments_count, image_path, tags FROM posts WHERE 1=1");
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (titleSubstring != null && !titleSubstring.isBlank()) {
            sql.append(" AND LOWER(title) LIKE :title");
            params.addValue("title", "%" + titleSubstring.toLowerCase(Locale.ROOT) + "%");
        }
        appendTagConditions(sql, requiredTags, params);
        sql.append(" ORDER BY created_at DESC LIMIT :limit OFFSET :offset");
        params.addValue("limit", limit);
        params.addValue("offset", offset);
        return jdbc.query(sql.toString(), params, mapper);
    }

    @Override
    public int countFiltered(String titleSubstring, List<String> requiredTags) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM posts WHERE 1=1");
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (titleSubstring != null && !titleSubstring.isBlank()) {
            sql.append(" AND LOWER(title) LIKE :title");
            params.addValue("title", "%" + titleSubstring.toLowerCase(Locale.ROOT) + "%");
        }
        appendTagConditions(sql, requiredTags, params);
        Integer result = jdbc.queryForObject(sql.toString(), params, Integer.class);
        return result == null ? 0 : result;
    }

    @Override
    public Optional<Integer> incrementLikesAndGet(Long id) {
        if (!existsById(id)) {
            return Optional.empty();
        }
        jdbc.update("UPDATE posts SET likes_count = likes_count + 1, updated_at = CURRENT_TIMESTAMP WHERE id=:id", Map.of("id", id));
        Integer value = jdbc.queryForObject("SELECT likes_count FROM posts WHERE id=:id", Map.of("id", id), Integer.class);
        return Optional.ofNullable(value);
    }

    @Override
    public void incrementCommentsCount(Long id) {
        jdbc.update("UPDATE posts SET comments_count = comments_count + 1 WHERE id=:id", Map.of("id", id));
    }

    @Override
    public void decrementCommentsCount(Long id) {
        jdbc.update("UPDATE posts SET comments_count = CASE WHEN comments_count > 0 THEN comments_count - 1 ELSE 0 END WHERE id=:id", Map.of("id", id));
    }

    @Override
    public boolean updateImagePath(Long id, String imagePath) {
        int updated = jdbc.update("UPDATE posts SET image_path=:image_path, updated_at=CURRENT_TIMESTAMP WHERE id=:id", Map.of("image_path", imagePath, "id", id));
        return updated > 0;
    }
}
