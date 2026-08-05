package com.example.demo.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String image;

    protected Category() {
    }

    public Category(String name) {
        this.name = name;
    }

    public Category(
            String name,
            String image
    ) {
        this.name = name;
        this.image = image;
    }

    public void applyDefaultImage(String image) {
        this.image = image;
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
}