package com.example.demo.service;

import com.example.demo.dto.category.CategoryListItemResponse;
import com.example.demo.dto.category.CategoryListResponse;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.PostCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final PostCategoryRepository postCategoryRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            PostCategoryRepository postCategoryRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.postCategoryRepository = postCategoryRepository;
    }

    public CategoryListResponse getCategoryList() {
        Map<Long, Long> postCountByCategoryId =
                postCategoryRepository
                        .countPostsByCategory()
                        .stream()
                        .collect(Collectors.toMap(
                                PostCategoryRepository.CategoryPostCount
                                        ::getCategoryId,
                                PostCategoryRepository.CategoryPostCount
                                        ::getPostCount
                        ));

        List<CategoryListItemResponse> categories =
                categoryRepository
                        .findAllByOrderByCategoryIdAsc()
                        .stream()
                        .map(category -> new CategoryListItemResponse(
                                category.getCategoryId(),
                                category.getName(),
                                category.getImage(),
                                postCountByCategoryId.getOrDefault(
                                        category.getCategoryId(),
                                        0L
                                )
                        ))
                        .toList();

        return new CategoryListResponse(categories);
    }
}