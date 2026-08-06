package com.example.demo.dto.category;

import java.util.List;

public class CategoryListResponse {

    private final List<CategoryListItemResponse> categories;

    public CategoryListResponse(
            List<CategoryListItemResponse> categories
    ) {
        this.categories = categories;
    }

    public List<CategoryListItemResponse> getCategories() {
        return categories;
    }
}