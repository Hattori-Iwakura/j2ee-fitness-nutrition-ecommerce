package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.service.CategoryService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/products")
    public String listProducts(@RequestParam(required = false) String category,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "0") int page,
                               Model model) {

        PageRequest pageable = PageRequest.of(page, 9, Sort.by("createdAt").descending());
        Page<Product> products;

        if (keyword != null && !keyword.isBlank()) {
            products = productService.searchByName(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else if (category != null && !category.isBlank()) {
            var cat = categoryService.findBySlug(category);
            if (cat.isPresent()) {
                products = productService.findByCategoryAndActive(cat.get().getId(), pageable);
                model.addAttribute("currentCategory", cat.get());
            } else {
                products = productService.findAllActive(pageable);
            }
            model.addAttribute("category", category);
        } else {
            products = productService.findAllActive(pageable);
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.findAllActive());
        return "product/list";
    }

    @GetMapping("/products/{slug}")
    public String productDetail(@PathVariable String slug, Model model) {
        Product product = productService.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        model.addAttribute("product", product);
        return "product/detail";
    }
}
