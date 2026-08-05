package com.example.demo.dto.category;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CategoryItemResponse {

    @JsonProperty("category_id")
    private Long categoryId;

    private String name;

    public CategoryItemResponse(
            Long categoryId,
            String name
    ) {
        this.categoryId = categoryId;
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }
}