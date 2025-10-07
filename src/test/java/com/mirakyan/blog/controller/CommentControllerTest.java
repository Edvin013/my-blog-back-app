package com.mirakyan.blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirakyan.blog.dto.CommentDto;
import com.mirakyan.blog.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommentControllerTest {

    private final CommentService commentService = Mockito.mock(CommentService.class);
    private final CommentController controller = new CommentController(commentService);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("GET /api/posts/{postId}/comments возвращает список")
    void getComments() throws Exception {
        when(commentService.getCommentsByPostId(1L))
                .thenReturn(List.of(
                CommentDto.builder()
                        .id(5L)
                        .text("Hi")
                        .postId(1L)
                        .build()
        ));
        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));
    }

    @Test
    @DisplayName("POST /api/posts/{postId}/comments создаёт и возвращает 201")
    void addComment() throws Exception {
        CommentDto response = CommentDto.builder()
                .id(10L)
                .text("Test")
                .postId(1L)
                .build();

        when(commentService.addCommentToPost(eq(1L), any(CommentDto.class))).thenReturn(Optional.of(response));

        CommentDto request = CommentDto.builder()
                .text("Test")
                .postId(1L)
                .build();
        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("PUT /api/posts/{postId}/comments/{id} обновляет комментарий")
    void updateComment() throws Exception {
        CommentDto response = CommentDto.builder()
                .id(3L)
                .text("Updated")
                .postId(2L)
                .build();
        when(commentService.updateComment(eq(2L), eq(3L), any(CommentDto.class)))
                .thenReturn(Optional.of(response));

        CommentDto request = CommentDto.builder()
                .id(3L)
                .text("Updated")
                .postId(2L)
                .build();

        mockMvc.perform(put("/api/posts/2/comments/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated"));
    }
}

