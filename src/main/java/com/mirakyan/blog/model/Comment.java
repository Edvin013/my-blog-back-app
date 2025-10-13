package com.mirakyan.blog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    private Long id;
    private String text;
    private Long postId;
    private Instant createdAt;
    private Instant updatedAt;

    public Comment(String text, Long postId) {
        this.text = text;
        this.postId = postId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}