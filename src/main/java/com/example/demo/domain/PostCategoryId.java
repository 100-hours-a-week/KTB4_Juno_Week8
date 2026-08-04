package com.example.demo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PostCategoryId implements Serializable {

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "category_id")
    private Long categoryId;

    protected PostCategoryId() {
    }

    public PostCategoryId(Long postId, Long categoryId) {
        this.postId = postId;
        this.categoryId = categoryId;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof PostCategoryId that)) {
            return false;
        }

        return Objects.equals(postId, that.postId)
                && Objects.equals(categoryId, that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, categoryId);
    }
}