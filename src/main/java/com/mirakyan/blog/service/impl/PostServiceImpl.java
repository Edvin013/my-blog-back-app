package com.mirakyan.blog.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirakyan.blog.dto.PostDto;
import com.mirakyan.blog.dto.PostsResponseDto;
import com.mirakyan.blog.model.Post;
import com.mirakyan.blog.repository.PostRepository;
import com.mirakyan.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {
    private static final String IMAGES_DIR = "images";
    private static final int MAX_PREVIEW_LENGTH = 128;

    private final PostRepository postRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    @Override
    public Optional<PostDto> getPostById(Long id) {
        return postRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    public PostDto createPost(PostDto postDto) {
        Post post = Post.builder()
                .title(postDto.getTitle())
                .text(postDto.getText())
                //.tags(postDto.getTags())
                // .tagsJson(convertTagsToJson(postDto.getTags()))
                .likesCount(0)
                .commentsCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Post savePost = postRepository.save(post);
        return convertToDto(savePost);
    }

    @Override
    public Optional<PostDto> updatePost(Long id, PostDto postDto) {
        return postRepository.findById(id).map(post -> {
            post.setTitle(postDto.getTitle());
            post.setText(postDto.getText());
            // post.setTags(postDto.getTags());
            // post.setTagsJson(convertTagsToJson(postDto.getTags()));
            post.setUpdatedAt(Instant.now());

            Post updateDTO = postRepository.save(post);
            return convertToDto(updateDTO);
        });
    }


    @Override
    public boolean deletePost(Long id) {
        if (postRepository.existsById(id)) {
            // todo commen delete
            postRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Optional<Integer> incrementLikes(Long id) {
        Optional<Post> postOptional = postRepository.findById(id);
        if (!postOptional.isPresent()) {
            return Optional.empty();
        }
        Post post = postOptional.get();
        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);
        return Optional.of(post.getLikesCount());
    }

    @Override
    public PostsResponseDto getAllPosts(String search, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);

        // todo
        // Временно используем простой поиск по всем постам
        // В production версии здесь будет более сложная логика поиска
        Page<Post> page = postRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<PostDto> posts = page.getContent().stream()
                .map(this::convertToDto)
                .map(this::truncateTextForPreview)
                .collect(Collectors.toList());

        return PostsResponseDto.builder()
                .posts(posts)
                .hasPrev(page.hasPrevious())
                .hasNext(page.hasNext())
                .lastPage(page.getTotalPages())
                .build();
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
            return objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {
            });
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

    private PostDto truncateTextForPreview(PostDto postDto) {
        if (postDto.getText() != null && postDto.getText().length() > MAX_PREVIEW_LENGTH) {
            postDto.setText(postDto.getText().substring(0, MAX_PREVIEW_LENGTH) + "…");
        }
        return postDto;
    }
}
