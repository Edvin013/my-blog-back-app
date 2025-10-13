package com.mirakyan.blog.service.impl;

import com.mirakyan.blog.dto.CommentDto;
import com.mirakyan.blog.model.Comment;
import com.mirakyan.blog.repository.CommentRepository;
import com.mirakyan.blog.service.CommentService;
import com.mirakyan.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final PostService postService;
    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Override
    public List<CommentDto> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId)
                .stream().map(this::convertToDto)
                .toList();
    }

    @Override
    public Optional<CommentDto> addCommentToPost(Long postId, CommentDto commentDto) {
        if (!postService.existsById(postId)) {
            return Optional.empty();
        }
        commentDto.setPostId(postId);
        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .postId(commentDto.getPostId())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        Comment savedComment = commentRepository.save(comment);
        postService.incrementCommentsCount(commentDto.getPostId());
        log.info("Добавлен комментарий id={} к посту id={}", savedComment.getId(), postId);
        return Optional.of(convertToDto(savedComment));
    }

    @Override
    public Optional<CommentDto> updateComment(Long postId, Long commentId, CommentDto commentDto) {
        return commentRepository.findById(commentId)
                .filter(comment -> comment.getPostId().equals(postId))
                .map(existingComment -> {
                    existingComment.setText(commentDto.getText());
                    existingComment.setUpdatedAt(Instant.now());
                    Comment updatedComment = commentRepository.save(existingComment);
                    log.info("Обновлён комментарий id={} поста id={}", commentId, postId);
                    return convertToDto(updatedComment);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommentDto> getCommentById(Long postId, Long commentId) {
        return commentRepository.findById(commentId)
                .filter(comment -> comment.getPostId().equals(postId))
                .map(this::convertToDto);
    }

    public boolean deleteComment(Long postId, Long commentId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isPresent() && commentOpt.get().getPostId().equals(postId)) {
            commentRepository.deleteById(commentId);
            postService.decrementCommentsCount(postId); // уменьшение счётчика
            log.info("Удалён комментарий id={} поста id={}", commentId, postId);
            return true;
        }
        return false;
    }

    private CommentDto convertToDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .postId(comment.getPostId())
                .build();
    }
}
