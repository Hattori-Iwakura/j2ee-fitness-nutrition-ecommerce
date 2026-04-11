package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.ProductFilter;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductSpecification;
import com.example.j2ee_fitness_nutrition_ecommerce.service.CategoryService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductServiceImpl(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Override
    public Page<Product> findAllActive(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    @Override
    public Page<Product> findByCategoryAndActive(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
    }

    @Override
    public Page<Product> searchByName(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword, pageable);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Optional<Product> findBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    @Override
    public Optional<Product> findActiveBySlug(String slug) {
        return productRepository.findBySlugAndActiveTrue(slug);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    public Page<Product> findWithFilter(ProductFilter filter, Pageable pageable) {
        Long categoryId = null;
        if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
            var cat = categoryService.findActiveBySlug(filter.getCategory());
            if (cat.isPresent()) {
                categoryId = cat.get().getId();
            }
        }
        return productRepository.findAll(ProductSpecification.withFilter(filter, categoryId), pageable);
    }

    @Override
    public List<String> findAllBrands() {
        return productRepository.findDistinctBrands();
    }

    @Override
    public List<Product> findRelatedProducts(Long productId, Long categoryId) {
        return productRepository.findTop4ByCategoryIdAndActiveTrueAndIdNot(categoryId, productId);
    }

    @Override
    public List<Product> findCoPurchasedProducts(Long productId) {
        return productRepository.findCoPurchasedProducts(productId, PageRequest.of(0, 4));
    }

    @Override
    public List<Product> findBestSellers(int limit) {
        return productRepository.findBestSellers(PageRequest.of(0, limit));
    }

    @Override
    public List<Product> findNewArrivals(int limit) {
        return productRepository.findByActiveTrueOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }
}
