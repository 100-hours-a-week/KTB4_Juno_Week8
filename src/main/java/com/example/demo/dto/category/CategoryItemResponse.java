package com.example.demo.dto.category;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CategoryItemResponse {

    @JsonProperty("category_id")
    private Long categoryId;

    private String name;
    private String image;

    public CategoryItemResponse(
            Long categoryId,
            String name,
            String image
    ) {
        this.categoryId = categoryId;
        this.name = name;
        this.image = image;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public String getImage(){
        return image;
    }
}