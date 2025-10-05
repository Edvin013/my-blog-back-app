package com.mirakyan.blog.controller;


import com.mirakyan.blog.dto.PostsResponseDto;
import com.mirakyan.blog.model.Post;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @GetMapping
    public ResponseEntity<PostsResponseDto> getPosts(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {


        PostsResponseDto response = PostsResponseDto.builder()
                .hasNext(false)
                .hasPrev(false)
                .lastPage(1)
                .posts(java.util.Collections.emptyList())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        // Placeholder implementation
        Post post = Post.builder()
                .id(id)
                .title("Sample Post")
                .text("This is a sample post content.")
                .build();
        return ResponseEntity.ok(post);
    }
}
