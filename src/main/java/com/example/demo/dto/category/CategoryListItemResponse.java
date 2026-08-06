package com.example.demo.dto.category;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CategoryListItemResponse {

    @JsonProperty("category_id")
    private final Long categoryId;

    private final String name;

    private final String image;

    @JsonProperty("post_count")
    private final Long postCount;

    public CategoryListItemResponse(
            Long categoryId,
            String name,
            String image,
            Long postCount
    ) {
        this.categoryId = categoryId;
        this.name = name;
        this.image = image;
        this.postCount = postCount;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public Long getPostCount() {
        return postCount;
    }
}