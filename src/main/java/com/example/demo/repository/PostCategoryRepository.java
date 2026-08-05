package com.example.demo.repository;

import com.example.demo.domain.Post;
import com.example.demo.domain.PostCategory;
import com.example.demo.domain.PostCategoryId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCategoryRepository
        extends JpaRepository<PostCategory, PostCategoryId> {

    void deleteAllByPost(Post post);

    @EntityGraph(attributePaths = "category")
    List<PostCategory> findAllByPost(Post post);

    @EntityGraph(attributePaths = {"post", "category"})
    List<PostCategory> findAllByPost_PostIdIn(List<Long> postIds);
}