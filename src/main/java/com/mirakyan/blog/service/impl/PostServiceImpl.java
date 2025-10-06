package com.mirakyan.blog.service.impl;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {


    private static final String IMAGES_DIR = "images"; // пока не используется
    private static final int MAX_PREVIEW_LENGTH = 128;

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    @Override
    public Optional<PostDto> getPostById(Long id) {
        return postRepository.findById(id)
                .map(this::convertToDto)
                .map(this::truncateTextForPreview);
    }

    @Override
    public PostDto createPost(PostDto postDto) {
        Post post = Post.builder()
                .title(postDto.getTitle())
                .text(postDto.getText())
                .tags(normalizeTagsToArray(postDto.getTags()))
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
            if (postDto.getTags() != null) {
                post.setTags(normalizeTagsToArray(postDto.getTags()));
            }
            post.setUpdatedAt(Instant.now());

            Post updateDTO = postRepository.save(post);
            return convertToDto(updateDTO);
        });
    }


    @Override
    public boolean deletePost(Long id) {
        if (postRepository.existsById(id)) {
            // todo comments delete handled by FK cascade
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

        // TODO внедрить реальный поиск по тегам и тексту
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

    @Override
    public boolean existsById(Long postId) {
        return postRepository.existsById(postId);
    }

    private String[] normalizeTagsToArray(List<String> tags) {
        if (tags == null) return new String[0];
        List<String> cleaned = tags.stream()
                .filter(t -> t != null && !t.trim().isEmpty())
                .map(t -> t.trim().toLowerCase())
                .distinct()
                .collect(Collectors.toList());
        return cleaned.toArray(new String[0]);
    }

    private List<String> arrayToList(String[] tags) {
        if (tags == null) return Collections.emptyList();
        return Arrays.asList(tags);
    }

    @Override
    public void incrementCommentsCount(Long id) {
        Post post = postRepository.findById(id).get();
        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);

    }

    @Override
    public void decrementCommentsCount(Long postId) {
        Post post = postRepository.findById(postId).get();
        post.setCommentsCount(Math.max(0, post.getCommentsCount() - 1));
        postRepository.save(post);
    }

    private PostDto convertToDto(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .text(post.getText())
                .tags(arrayToList(post.getTags()))
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
