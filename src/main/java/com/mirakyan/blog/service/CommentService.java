package com.mirakyan.blog.service;

import com.mirakyan.blog.dto.CommentDto;

import java.util.List;

public interface CommentService {
    List<CommentDto> getCommentsByPostId(Long postId);
}
