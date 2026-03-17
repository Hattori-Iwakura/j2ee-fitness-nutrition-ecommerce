package com.example.j2ee_fitness_nutrition_ecommerce.service;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> findAllActive();
    List<Category> findAll();
    Optional<Category> findById(Long id);
    Optional<Category> findBySlug(String slug);
    Optional<Category> findActiveBySlug(String slug);
    Category save(Category category);
    void deleteById(Long id);
}
