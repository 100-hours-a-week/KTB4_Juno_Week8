package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.category.CategoryListResponse;
import com.example.demo.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(
            CategoryService categoryService
    ) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CategoryListResponse>>
    getCategoryList() {
        CategoryListResponse response =
                categoryService.getCategoryList();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "카테고리 목록 조회에 성공하였습니다.",
                        response
                ));
    }
}