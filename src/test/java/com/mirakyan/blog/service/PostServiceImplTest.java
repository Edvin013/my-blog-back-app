package com.mirakyan.blog.service;

import com.mirakyan.blog.dto.PostDto;
import com.mirakyan.blog.dto.PostsResponseDto;
import com.mirakyan.blog.model.Post;
import com.mirakyan.blog.repository.PostRepository;
import com.mirakyan.blog.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostServiceImpl postService;

    private Post existing;

    @BeforeEach
    void setUp() {
        existing = Post.builder()
                .id(1L)
                .title("Title 1")
                .text("Some long text")
                .likesCount(2)
                .commentsCount(1)
                .tags(new String[]{"java", "spring"})
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("createPost: должен сохранить пост и вернуть DTO с начальными счетчиками")
    void createPost() {
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        PostDto request = PostDto.builder()
                .title("New Title")
                .text("Body")
                .tags(List.of("Java", "java", "SPRING"))
                .build();

        PostDto result = postService.createPost(request);

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post saved = captor.getValue();

        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getLikesCount()).isZero();
        assertThat(saved.getCommentsCount()).isZero();
        assertThat(Arrays.asList(saved.getTags())).containsExactlyInAnyOrder("java", "spring");
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getLikesCount()).isZero();
    }

    @Test
    @DisplayName("updatePost: обновляет поля и теги")
    void updatePost() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        PostDto update = PostDto.builder()
                .title("Updated")
                .text("Updated body")
                .tags(List.of("new", "tags"))
                .build();

        Optional<PostDto> result = postService.updatePost(1L, update);
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Updated");
        assertThat(result.get().getTags()).containsExactlyInAnyOrder("new", "tags");
    }

    @Test
    @DisplayName("incrementLikes: увеличивает счётчик и возвращает новое значение")
    void incrementLikes() {
        when(postRepository.incrementLikesAndGet(1L)).thenReturn(Optional.of(3));
        Optional<Integer> newLikes = postService.incrementLikes(1L);
        assertThat(newLikes).contains(3);
    }

    @Test
    @DisplayName("incrementLikes: пост не найден => empty")
    void incrementLikesNotFound() {
        when(postRepository.incrementLikesAndGet(99L)).thenReturn(Optional.empty());
        assertThat(postService.incrementLikes(99L)).isEmpty();
    }

    @Test
    @DisplayName("deletePost: существующий => true")
    void deletePostTrue() {
        when(postRepository.existsById(1L)).thenReturn(true);
        doNothing().when(postRepository).deleteById(1L);
        assertThat(postService.deletePost(1L)).isTrue();
        verify(postRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deletePost: отсутствует => false")
    void deletePostFalse() {
        when(postRepository.existsById(2L)).thenReturn(false);
        assertThat(postService.deletePost(2L)).isFalse();
        verify(postRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("getAllPosts: фильтрация по тегам и пагинация через БД")
    void getAllPostsFilteringAndPagination() {
        Post p1 = Post.builder().id(1L).title("Spring Data Intro").text("T".repeat(150)).likesCount(0).commentsCount(0)
                .tags(new String[]{"spring", "data"}).createdAt(Instant.now()).updatedAt(Instant.now()).build();
        Post p2 = Post.builder().id(2L).title("Java Streams Guide").text("Short text").likesCount(0).commentsCount(0)
                .tags(new String[]{"java"}).createdAt(Instant.now().minusSeconds(10)).updatedAt(Instant.now()).build();

        // Сценарий 1: поиск по тегам #spring #data
        when(postRepository.countFiltered(eq(""), eq(List.of("spring", "data")))).thenReturn(1);
        when(postRepository.findFiltered(eq(""), eq(List.of("spring", "data")), eq(0), eq(10))).thenReturn(List.of(p1));
        PostsResponseDto onlyP1 = postService.getAllPosts("#spring  #data", 1, 10);
        assertThat(onlyP1.getPosts()).hasSize(1);
        assertThat(onlyP1.getPosts().get(0).getId()).isEqualTo(1L);
        assertThat(onlyP1.getPosts().get(0).getText().length()).isEqualTo(128 + 1); // обрезка + …

        // Сценарий 2: пустой поиск, две записи, пагинация размером 1
        when(postRepository.countFiltered(eq(""), isNull())).thenReturn(2);
        when(postRepository.findFiltered(eq(""), isNull(), eq(1), eq(1))).thenReturn(List.of(p2)); // offset=1 (вторая страница)
        PostsResponseDto page2 = postService.getAllPosts("", 2, 1);
        assertThat(page2.getPosts()).hasSize(1);
        assertThat(page2.getPosts().get(0).getId()).isEqualTo(2L);
        assertThat(page2.isHasPrev()).isTrue();
        assertThat(page2.isHasNext()).isFalse();
        assertThat(page2.getLastPage()).isEqualTo(2);
    }
}
