package com.mirakyan.blog.controller;


import com.mirakyan.blog.dto.PostDto;
import com.mirakyan.blog.dto.PostsResponseDto;
import com.mirakyan.blog.model.Post;
import com.mirakyan.blog.service.PostServece;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostServece postService;

    @GetMapping
    public ResponseEntity<PostsResponseDto> getPosts(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {

        PostDto post = PostDto.builder()
                .id(1L)
                .title("Sample Post")
                .text("This is a sample post content.")
                .tags(List.of("sample", "post"))
                .likesCount(10)
                .commentsCount(5)
                .build();
        PostsResponseDto response = PostsResponseDto.builder()
                .hasNext(false)
                .hasPrev(false)
                .lastPage(1)
                .posts(List.of(post))
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        // Placeholder implementation
        Post post = Post.builder()
                .id(id)
                .title("Sample Post")
                .text("This is a sample post content.")
                .build();
        return ResponseEntity.ok(post);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<String> getPostImage(@PathVariable Long id) {
        // Placeholder implementation - возвращаем URL изображения-заглушки
        String imageUrl = "https://image.winudf.com/v2/image/bW9iaS5hbmRyb2FwcC5wcm9zcGVyaXR5YXBwcy5jNTExMV9zY3JlZW5fN18xNTI0MDQxMDUwXzAyMQ/screen-7.jpg?fakeurl=1&type=.jpg";
        return ResponseEntity.ok(imageUrl);
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody PostDto postDto) {
        PostDto createdPost = postService.createPost(postDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }
}
