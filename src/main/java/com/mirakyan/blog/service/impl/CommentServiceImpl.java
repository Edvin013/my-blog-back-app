package com.mirakyan.blog.service.impl;

import com.mirakyan.blog.dto.CommentDto;
import com.mirakyan.blog.model.Comment;
import com.mirakyan.blog.repository.CommentRepository;
import com.mirakyan.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;


    @Override
    public List<CommentDto> getCommentsByPostId(Long postId) {
        return commentRepository.findCommentByPostId(postId)
                .stream().map(this::convertToDto)
                .toList();
    }

    private CommentDto convertToDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .postId(comment.getPostId())
                .build();
    }
}
