package com.mirakyan.blog.controller;


import com.mirakyan.blog.dto.PostDto;
import com.mirakyan.blog.dto.PostsResponseDto;
import com.mirakyan.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<PostsResponseDto> getPosts(
            @RequestParam(name = "search") String search,
            @RequestParam(name = "pageNumber") int pageNumber,
            @RequestParam(name = "pageSize") int pageSize) {

        PostsResponseDto response = postService.getAllPosts(search, pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPostById(@PathVariable Long id) {
        return postService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Вариант A из ТЗ: добавляем POST /api/posts/{id} для совместимости
    @PostMapping("/{id}")
    public ResponseEntity<PostDto> getPostByIdViaPost(@PathVariable Long id) { // возвращает то же что и GET
        return getPostById(id);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getPostImage(@PathVariable Long id) {
        if (!postService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Optional<byte[]> imageOpt = postService.getImage(id);
        if (!imageOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 если изображения нет
        }
        byte[] bytes = imageOpt.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); // TODO: определять content-type по расширени��
        headers.setContentLength(bytes.length);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateImage(@PathVariable Long id, @RequestParam("image") MultipartFile image) {
        if (image == null || image.isEmpty()) {
            // Пустой файл => 400 Bad Request (уточнено в требованиях)
            return ResponseEntity.badRequest().build();
        }
        boolean updated = postService.updateImage(id, image);
        if (updated) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build(); // пост не найден
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody PostDto postDto) { // @Valid
        PostDto createdPost = postService.createPost(postDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    @PostMapping("/{id}/likes")
    public ResponseEntity<Integer> incrementLikes(@PathVariable  Long id) {
        return postService.incrementLikes(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id, @Valid @RequestBody PostDto postDto) { // @Valid
        return postService.updatePost(id, postDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        if (postService.deletePost(id)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
