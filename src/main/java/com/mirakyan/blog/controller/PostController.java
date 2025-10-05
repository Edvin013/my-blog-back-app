package com.mirakyan.blog.controller;

import com.mirakyan.blog.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    @GetMapping
    public ResponseEntity<Post> hello(){
        return ResponseEntity.ok(Post.builder().id(1L).title("Hello").text("Content").build());
    }
}
