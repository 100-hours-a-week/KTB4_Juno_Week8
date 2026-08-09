package com.example.demo.config;

import com.example.demo.domain.Category;
import com.example.demo.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void run(String... args) {
        List<CategoryDefaultData> categoryDefaultData = List.of(
                new CategoryDefaultData(
                        "얼얼한 매운맛",
                        "categories/spicy.webp"
                ),
                new CategoryDefaultData(
                        "달콤고소한 맛",
                        "categories/sweet-nutty.webp"
                ),
                new CategoryDefaultData(
                        "새콤상큼한 맛",
                        "categories/sour-fresh.webp"
                ),
                new CategoryDefaultData(
                        "짭짤한 간장 맛",
                        "categories/soy-salty.webp"
                ),
                new CategoryDefaultData(
                        "고소한 참깨 맛",
                        "categories/sesame-nutty.webp"
                ),
                new CategoryDefaultData(
                        "연예인 추천 조합",
                        "categories/celebrity-pick.webp"
                )
        );

        List<Category> categories = categoryRepository.findAll();

        for (CategoryDefaultData defaultData : categoryDefaultData) {
            Category category = categories.stream()
                    .filter(currentCategory ->
                            currentCategory.getName()
                                    .equals(defaultData.name())
                    )
                    .findFirst()
                    .orElseGet(() ->
                            categoryRepository.save(
                                    new Category(
                                            defaultData.name(),
                                            defaultData.image()
                                    )
                            )
                    );

            category.applyDefaultImage(defaultData.image());
        }
    }

    private record CategoryDefaultData(
            String name,
            String image
    ) {
    }
}