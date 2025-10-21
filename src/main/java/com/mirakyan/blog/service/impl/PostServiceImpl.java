package com.mirakyan.blog.service.impl;

import com.mirakyan.blog.dto.PostDto;
import com.mirakyan.blog.dto.PostsResponseDto;
import com.mirakyan.blog.model.Post;
import com.mirakyan.blog.repository.PostRepository;
import com.mirakyan.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    private static final Logger log = LoggerFactory.getLogger(PostServiceImpl.class);

    private static final String IMAGES_DIR = "images";
    private static final int MAX_PREVIEW_LENGTH = 128;

    private final PostRepository postRepository;

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
                .tags(normalizeTagsToArray(postDto.getTags()))
                .likesCount(0)
                .commentsCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Post savePost = postRepository.save(post);
        log.info("Создан пост id={} title='{}'", savePost.getId(), savePost.getTitle());
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
            log.info("Обновлён пост id={}", id);
            return convertToDto(updateDTO);
        });
    }

    @Override
    public boolean deletePost(Long id) {
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
            log.info("Удалён пост id={}", id);
            return true;
        }
        return false;
    }

    @Override
    public Optional<Integer> incrementLikes(Long id) {
        return postRepository.incrementLikesAndGet(id);
    }

    @Override
    public PostsResponseDto getAllPosts(String search, int pageNumber, int pageSize) {
        if (pageNumber < 1) pageNumber = 1;
        if (pageSize < 1) pageSize = 1;

        String rawSearch = search == null ? "" : search.trim();
        List<String> tokens = rawSearch.isEmpty() ? List.of() : Arrays.stream(rawSearch.split("\\s+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        List<String> tagTokens = new ArrayList<>();
        List<String> titleTokens = new ArrayList<>();
        for (String token : tokens) {
            if (token.startsWith("#") && token.length() > 1) {
                tagTokens.add(token.substring(1).toLowerCase(Locale.ROOT));
            } else if (!token.startsWith("#")) {
                titleTokens.add(token);
            }
        }
        String titleSubstring = titleTokens.isEmpty() ? "" : String.join(" ", titleTokens).toLowerCase(Locale.ROOT);

        int total = postRepository.countFiltered(titleSubstring, tagTokens.isEmpty() ? null : tagTokens);
        int lastPage = total == 0 ? 0 : (int) Math.ceil(total / (double) pageSize);
        if (lastPage != 0 && pageNumber > lastPage) {
            pageNumber = lastPage;
        }
        int offset = (pageNumber - 1) * pageSize;
        List<Post> page = total == 0 ? Collections.emptyList() : postRepository.findFiltered(titleSubstring, tagTokens.isEmpty() ? null : tagTokens, offset, pageSize);

        List<PostDto> slice = page.stream()
                .map(this::convertToDto)
                .map(this::truncateTextForPreview)
                .toList();

        boolean hasPrev = pageNumber > 1 && lastPage > 0;
        boolean hasNext = pageNumber < lastPage;

        return PostsResponseDto.builder()
                .posts(slice)
                .hasPrev(hasPrev)
                .hasNext(hasNext)
                .lastPage(lastPage)
                .build();
    }

    @Override
    public boolean existsById(Long postId) {
        return postRepository.existsById(postId);
    }

    private String[] normalizeTagsToArray(List<String> tags) {
        if (tags == null) {
            return new String[0];
        }

        List<String> cleaned = tags.stream()
                .filter(t -> t != null && !t.trim().isEmpty())
                .map(t -> t.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
        return cleaned.toArray(new String[0]);
    }

    private List<String> arrayToList(String[] tags) {
        if (tags == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(tags);
    }

    @Override
    public void incrementCommentsCount(Long id) {
        postRepository.incrementCommentsCount(id);
    }

    @Override
    public void decrementCommentsCount(Long postId) {
        postRepository.decrementCommentsCount(postId);
    }

    @Override
    public boolean updateImage(Long id, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return false;
        }
        if (!postRepository.existsById(id)) {
            return false;
        }
        try {
            String original = image.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            Files.createDirectories(Paths.get(IMAGES_DIR));
            String fileName = "post-" + id + ext;
            Path target = Paths.get(IMAGES_DIR, fileName).toAbsolutePath();
            Files.write(target, image.getBytes());
            boolean updated = postRepository.updateImagePath(id, target.toString());
            if (updated) {
                log.info("Обновлено изображение поста id={}", id);
            }
            return updated;
        } catch (IOException e) {
            log.warn("Ошибка сохранения изображения поста id={}: {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<byte[]> getImage(Long id) {
        return postRepository.findById(id).flatMap(post -> {
            String pathStr = post.getImagePath();
            if (pathStr == null) {
                return Optional.empty();
            }
            Path path = Paths.get(pathStr);
            if (!Files.exists(path)) {
                return Optional.empty();
            }
            try {
                return Optional.of(Files.readAllBytes(path));
            } catch (IOException e) {
                log.warn("Не удалось прочитать изображение поста id={}: {}", id, e.getMessage());
                return Optional.empty();
            }
        });
    }

    private PostDto convertToDto(Post post) {
        Integer likes = post.getLikesCount() == null ? 0 : post.getLikesCount();
        Integer comments = post.getCommentsCount() == null ? 0 : post.getCommentsCount();
        List<String> tags = arrayToList(post.getTags());
        if (tags == null) {
            tags = Collections.emptyList();
        }
        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .text(post.getText())
                .tags(tags)
                .likesCount(likes)
                .commentsCount(comments)
                .build();
    }

    private PostDto truncateTextForPreview(PostDto postDto) {
        if (postDto.getText() != null && postDto.getText().length() > MAX_PREVIEW_LENGTH) {
            postDto.setText(postDto.getText().substring(0, MAX_PREVIEW_LENGTH) + "…");
        }
        return postDto;
    }
}
