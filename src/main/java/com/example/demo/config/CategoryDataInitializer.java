package com.example.demo.config;

import com.example.demo.domain.Category;
import com.example.demo.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryDataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public CategoryDataInitializer(
            CategoryRepository categoryRepository
    ) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        List<String> categoryNames = List.of(
                "얼얼한 매운맛",
                "달콤고소한 맛",
                "새콤상큼한 맛",
                "짭짤한 간장 맛",
                "고소한 참깨 맛",
                "연예인 추천 조합"
        );

        for (String categoryName : categoryNames) {
            if (!categoryRepository.existsByName(categoryName)) {
                categoryRepository.save(
                        new Category(categoryName)
                );
            }
        }
    }
}