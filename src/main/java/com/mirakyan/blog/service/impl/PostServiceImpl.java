package com.mirakyan.blog.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirakyan.blog.dto.PostDto;
import com.mirakyan.blog.model.Post;
import com.mirakyan.blog.repository.PostRepository;
import com.mirakyan.blog.service.PostServece;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostServece {

    private final PostRepository postRepository;
    private final ObjectMapper objectMapper;

    @Override
    public PostDto createPost(PostDto postDto) {
        Post post = Post.builder()
                .title(postDto.getTitle())
                .text(postDto.getText())
                .tags(postDto.getTags())
                .tagsJson(convertTagsToJson(postDto.getTags()))
                .likesCount(0)
                .commentsCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Post savePost = postRepository.save(post);
        return convertToDto(savePost);
    }

    private String convertTagsToJson(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<String> convertTagsFromJson(String tagsJson) {
        if (tagsJson == null || tagsJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    private PostDto convertToDto(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .text(post.getText())
                .tags(convertTagsFromJson(post.getTagsJson()))
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .build();
    }
}
