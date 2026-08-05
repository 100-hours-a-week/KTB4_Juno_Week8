package com.example.demo.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "post_categories")
public class PostCategory {

    @EmbeddedId
    private PostCategoryId id;

    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @MapsId("categoryId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    protected PostCategory() {
    }

    public PostCategory(Post post, Category category) {
        this.post = post;
        this.category = category;
        this.id = new PostCategoryId(
                post.getPostId(),
                category.getCategoryId()
        );
    }

    public PostCategoryId getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public Category getCategory() {
        return category;
    }
}