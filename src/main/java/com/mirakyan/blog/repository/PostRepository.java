package com.mirakyan.blog.repository;

import com.mirakyan.blog.model.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Optional<Post> findById(Long id);
    Post save(Post post);
    boolean existsById(Long id);
    void deleteById(Long id);

    // Пагинация + фильтрация по подстроке заголовка и обязательному наличию всех тегов
    List<Post> findFiltered(String titleSubstring, List<String> requiredTags, int offset, int limit);
    int countFiltered(String titleSubstring, List<String> requiredTags);

    Optional<Integer> incrementLikesAndGet(Long id);
    void incrementCommentsCount(Long id);
    void decrementCommentsCount(Long id);
    boolean updateImagePath(Long id, String imagePath);
}
