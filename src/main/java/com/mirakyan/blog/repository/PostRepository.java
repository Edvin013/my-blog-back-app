package com.mirakyan.blog.repository;

import com.mirakyan.blog.model.Post;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends CrudRepository<Post, Long> {
    // Пагинация и сортировка реализованы вручную в сервисе (см. PostServiceImpl).
}
