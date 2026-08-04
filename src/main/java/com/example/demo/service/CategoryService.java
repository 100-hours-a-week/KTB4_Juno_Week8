package com.example.demo.service;

import com.example.demo.dto.category.CategoryItemResponse;
import com.example.demo.dto.category.CategoryListResponse;
import com.example.demo.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(
            CategoryRepository categoryRepository
    ) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryListResponse getCategoryList() {
        List<CategoryItemResponse> categories =
                categoryRepository
                        .findAllByOrderByCategoryIdAsc()
                        .stream()
                        .map(category -> new CategoryItemResponse(
                                category.getCategoryId(),
                                category.getName()
                        ))
                        .toList();

        return new CategoryListResponse(categories);
    }
}