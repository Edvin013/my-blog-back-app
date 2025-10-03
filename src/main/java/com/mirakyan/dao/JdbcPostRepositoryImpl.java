package com.mirakyan.dao;

import com.mirakyan.blog.model.Post;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcPostRepositoryImpl implements PostRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcPostRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Post> findById(Long id) {
        return Optional.empty();
    }
}
