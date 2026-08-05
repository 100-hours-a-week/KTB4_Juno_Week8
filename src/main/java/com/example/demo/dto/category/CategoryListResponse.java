package com.example.demo.dto.category;

import java.util.List;

public class CategoryListResponse {

    private List<CategoryItemResponse> categories;

    public CategoryListResponse(
            List<CategoryItemResponse> categories
    ) {
        this.categories = categories;
    }

    public List<CategoryItemResponse> getCategories() {
        return categories;
    }
}