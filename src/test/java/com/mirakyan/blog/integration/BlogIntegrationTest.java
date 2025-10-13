package com.mirakyan.blog.integration;

import com.mirakyan.blog.config.DatabaseConfiguration;
import com.mirakyan.blog.config.WebConfiguration;
import com.mirakyan.blog.dto.CommentDto;
import com.mirakyan.blog.dto.PostDto;
import com.mirakyan.blog.dto.PostsResponseDto;
import com.mirakyan.blog.service.CommentService;
import com.mirakyan.blog.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebConfiguration.class, DatabaseConfiguration.class})
@Transactional
@Sql(scripts = "classpath:schema.sql")
class BlogIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Test
    @DisplayName("H2: end-to-end сценарий посты + пагинация + лайк + комментарий")
    void fullScenario() {
        PostDto p1 = postService.createPost(PostDto.builder()
                .title("First Post")
                .text("Body1" + "x".repeat(200))
                .tags(List.of("tag1", "tag2"))
                .build());
        PostDto p2 = postService.createPost(PostDto.builder()
                .title("Second Post")
                .text("Body2")
                .tags(List.of("tag2"))
                .build());

        assertThat(p1.getId()).isNotNull();
        assertThat(p2.getId()).isNotNull();

        assertThat(p1.getTags()).isNotNull();

        PostsResponseDto page1 = postService.getAllPosts("", 1, 1);
        assertThat(page1.getPosts()).hasSize(1);
        assertThat(page1.getLastPage()).isEqualTo(2);

        assertThat(postService.incrementLikes(p1.getId())).contains(1);

        CommentDto added = commentService.addCommentToPost(p1.getId(),
                        CommentDto.builder()
                                .text("Great!")
                                .postId(p1.getId())
                                .build())
                .orElseThrow();
        assertThat(added.getId()).isNotNull();
        PostDto refreshed = postService.getPostById(p1.getId()).orElseThrow();
        assertThat(refreshed.getCommentsCount()).isEqualTo(1);

        boolean deleted = commentService.deleteComment(p1.getId(), added.getId());
        assertThat(deleted).isTrue();
        PostDto refreshed2 = postService.getPostById(p1.getId()).orElseThrow();
        assertThat(refreshed2.getCommentsCount()).isZero();
    }
}
