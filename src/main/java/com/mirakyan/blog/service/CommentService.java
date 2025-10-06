package com.mirakyan.blog.service;

import com.mirakyan.blog.dto.CommentDto;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    List<CommentDto> getCommentsByPostId(Long postId);

    Optional<CommentDto> addCommentToPost(Long postId, CommentDto commentDto);

    Optional<CommentDto> updateComment(Long postId, Long commentId, CommentDto commentDto);

    Optional<CommentDto> getCommentById(Long postId, Long commentId);

    boolean deleteComment(Long postId, Long commentId);
}
