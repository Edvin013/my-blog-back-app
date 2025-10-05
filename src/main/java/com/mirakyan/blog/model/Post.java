package com.mirakyan.blog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table("posts")
public class Post {
    @Id
    private Long id;

    @Column("title")
    private String title;

    @Column("text")
    private String text;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    @Column("likes_count")
    private Integer likesCount;

    @Column("comments_count")
    private Integer commentsCount;

    @Column("image_path")
    private String imagePath;

    @Column("tags")
    private String tagsJson;


    private List<String> tags;

    public Post(String title, String text, List<String> tags) {
        this.title = title;
        this.text = text;
        this.tags = tags;
        this.likesCount = 0;
        this.commentsCount = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
