package com.mirakyan.blog.service;

import com.mirakyan.blog.dto.PostDto;
import com.mirakyan.blog.dto.PostsResponseDto;

import java.util.Optional;

public interface PostService {
    Optional<PostDto> getPostById(Long id);

    PostDto createPost(PostDto postDto);

    Optional<PostDto> updatePost(Long id, PostDto postDto);

    boolean deletePost(Long id);

    Optional<Integer> incrementLikes(Long id);

    PostsResponseDto getAllPosts(String search, int pageNumber, int pageSize);

    boolean existsById(Long postId);

    void incrementCommentsCount(Long postId);
}
