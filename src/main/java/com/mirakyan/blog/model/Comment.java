package com.mirakyan.blog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("comments")
public class Comment {

    @Id
    private Long id;

    @Column("text")
    private String text;

    @Column("post_id")
    private Long postId;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    public Comment(String text, Long postId) {
        this.text = text;
        this.postId = postId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}