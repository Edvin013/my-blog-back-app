package com.mirakyan.blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirakyan.blog.dto.PostDto;
import com.mirakyan.blog.dto.PostsResponseDto;
import com.mirakyan.blog.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/posts возвращает список постов и метаданные пагинации")
    void getPosts() throws Exception {
        PostsResponseDto dto = PostsResponseDto.builder()
                .posts(List.of(PostDto.builder()
                        .id(1L)
                        .title("T1")
                        .text("Body")
                        .tags(List.of())
                        .likesCount(0)
                        .commentsCount(0)
                        .build()))
                .hasPrev(false).hasNext(false).lastPage(1)
                .build();
        when(postService.getAllPosts(eq("") , eq(1), eq(5))).thenReturn(dto);

        mockMvc.perform(get("/api/posts")
                        .param("search","" )
                        .param("pageNumber","1")
                        .param("pageSize","5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].id").value(1))
                .andExpect(jsonPath("$.lastPage").value(1));
    }

    @Test
    @DisplayName("GET /api/posts/{id} 404 если не найден")
    void getPostNotFound() throws Exception {
        when(postService.getPostById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/posts/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/posts создаёт пост и возвращает 201")
    void createPost() throws Exception {
        PostDto request = PostDto.builder()
                .title("New")
                .text("Body")
                .tags(List.of())
                .build();

        PostDto response = PostDto.builder()
                .id(10L)
                .title("New")
                .text("Body")
                .tags(List.of())
                .likesCount(0)
                .commentsCount(0)
                .build();

        when(postService.createPost(any(PostDto.class))).thenReturn(response);
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("POST /api/posts/{id}/likes инкрементирует лайки")
    void incrementLikes() throws Exception {
        when(postService.incrementLikes(1L)).thenReturn(Optional.of(5));
        mockMvc.perform(post("/api/posts/1/likes"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }
}

