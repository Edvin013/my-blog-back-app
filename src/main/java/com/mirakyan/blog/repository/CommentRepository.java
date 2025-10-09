package com.mirakyan.blog.repository;

import com.mirakyan.blog.model.Comment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long> {
    List<Comment> findCommentByPostId(Long postId);
}
