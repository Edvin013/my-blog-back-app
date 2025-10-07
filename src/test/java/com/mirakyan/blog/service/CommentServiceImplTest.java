package com.mirakyan.blog.service;

import com.mirakyan.blog.dto.CommentDto;
import com.mirakyan.blog.model.Comment;
import com.mirakyan.blog.repository.CommentRepository;
import com.mirakyan.blog.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostService postService;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    @DisplayName("addCommentToPost: пост не существует -> empty")
    void addCommentPostNotFound() {
        when(postService.existsById(100L)).thenReturn(false);
        assertThat(commentService.addCommentToPost(100L, CommentDto.builder().text("Hi").postId(100L).build())).isEmpty();
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("addCommentToPost: успешное добавление + инкремент счётчика")
    void addCommentSuccess() {
        when(postService.existsById(1L)).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        CommentDto dto = CommentDto.builder().text("First").postId(1L).build();
        var saved = commentService.addCommentToPost(1L, dto);
        assertThat(saved).isPresent();
        assertThat(saved.get().getId()).isEqualTo(10L);
        verify(postService).incrementCommentsCount(1L);
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getText()).isEqualTo("First");
    }

    @Test
    @DisplayName("updateComment: обновляет текст если принадлежит посту")
    void updateComment() {
        Comment existing = Comment.builder().id(5L).text("Old").postId(3L).createdAt(Instant.now()).updatedAt(Instant.now()).build();
        when(commentRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        var updated = commentService.updateComment(3L, 5L, CommentDto.builder().text("New").postId(3L).build());
        assertThat(updated).isPresent();
        assertThat(updated.get().getText()).isEqualTo("New");
    }

    @Test
    @DisplayName("updateComment: возвращает empty если комментарий другого поста")
    void updateCommentWrongPost() {
        Comment existing = Comment.builder().id(5L).text("Old").postId(7L).createdAt(Instant.now()).updatedAt(Instant.now()).build();
        when(commentRepository.findById(5L)).thenReturn(Optional.of(existing));
        assertThat(commentService.updateComment(3L, 5L, CommentDto.builder().text("New").postId(3L).build())).isEmpty();
    }

    @Test
    @DisplayName("deleteComment: успешное удаление -> true и decrement")
    void deleteComment() {
        Comment existing = Comment.builder()
                .id(9L)
                .text("Bye")
                .postId(2L)
                .createdAt(Instant.now())
                .updatedAt(Instant.now()).build();

        when(commentRepository.findById(9L)).thenReturn(Optional.of(existing));
        doNothing().when(commentRepository).deleteById(9L);
        boolean result = commentService.deleteComment(2L, 9L);
        assertThat(result).isTrue();
        verify(postService).decrementCommentsCount(2L);
    }

    @Test
    @DisplayName("deleteComment: не найден или чужой пост -> false")
    void deleteCommentNotFound() {
        when(commentRepository.findById(9L)).thenReturn(Optional.empty());
        assertThat(commentService.deleteComment(2L, 9L)).isFalse();
        verify(postService, never()).decrementCommentsCount(anyLong());
    }

    @Test
    @DisplayName("getCommentsByPostId: маппинг сущностей в DTO")
    void getCommentsByPostId() {
        Comment c1 = Comment.builder().id(1L).text("A").postId(11L).createdAt(Instant.now()).updatedAt(Instant.now()).build();
        Comment c2 = Comment.builder().id(2L).text("B").postId(11L).createdAt(Instant.now()).updatedAt(Instant.now()).build();
        when(commentRepository.findCommentByPostId(11L)).thenReturn(List.of(c1, c2));
        var list = commentService.getCommentsByPostId(11L);
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getPostId()).isEqualTo(11L);
    }
}

