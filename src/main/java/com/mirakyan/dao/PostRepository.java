package com.mirakyan.dao;

import com.mirakyan.blog.model.Post;

import java.util.Optional;

public interface PostRepository {
    Optional<Post> findById(Long id);
}
