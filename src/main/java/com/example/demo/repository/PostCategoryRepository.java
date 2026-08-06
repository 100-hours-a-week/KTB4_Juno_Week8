package com.example.demo.repository;

import com.example.demo.domain.Post;
import com.example.demo.domain.PostCategory;
import com.example.demo.domain.PostCategoryId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostCategoryRepository
        extends JpaRepository<PostCategory, PostCategoryId> {

    void deleteAllByPost(Post post);

    @EntityGraph(attributePaths = "category")
    List<PostCategory> findAllByPost(Post post);

    @EntityGraph(attributePaths = {"post", "category"})
    List<PostCategory> findAllByPost_PostIdIn(List<Long> postIds);

    @Query("""
            select
                pc.category.categoryId as categoryId,
                count(pc.post.postId) as postCount
            from PostCategory pc
            where pc.post.deletedAt is null
            group by pc.category.categoryId
            """)

    List<CategoryPostCount> countPostsByCategory();

    interface CategoryPostCount {

        Long getCategoryId();

        Long getPostCount();
    }
}