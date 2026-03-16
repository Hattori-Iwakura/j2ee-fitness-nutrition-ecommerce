package com.example.j2ee_fitness_nutrition_ecommerce.service;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Page<Product> findAllActive(Pageable pageable);
    Page<Product> findByCategoryAndActive(Long categoryId, Pageable pageable);
    Page<Product> searchByName(String keyword, Pageable pageable);
    Optional<Product> findById(Long id);
    Optional<Product> findBySlug(String slug);
    List<Product> findAll();
    Product save(Product product);
    void deleteById(Long id);
}
