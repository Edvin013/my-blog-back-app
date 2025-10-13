package com.mirakyan.blog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Post {
    private Long id; // id задаётся БД (BIGSERIAL / IDENTITY)

    private String title;
    private String text;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer likesCount;
    private Integer commentsCount;
    private String imagePath;
    private String[] tags;

    public Post(String title, String text, List<String> tags) {
        this.title = title;
        this.text = text;
        this.likesCount = 0;
        this.commentsCount = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (tags != null) {
            this.tags = tags.stream().distinct().toArray(String[]::new);
        } else {
            this.tags = new String[0];
        }
    }
}
