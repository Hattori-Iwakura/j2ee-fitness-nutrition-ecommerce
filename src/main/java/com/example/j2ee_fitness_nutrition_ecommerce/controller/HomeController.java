package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.repository.CategoryRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    public HomeController(CategoryRepository categoryRepository, ProductService productService) {
        this.categoryRepository = categoryRepository;
        this.productService = productService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryRepository.findByActiveTrue());
        model.addAttribute("featuredProducts", productService.findNewArrivals(8));
        model.addAttribute("bestSellers", productService.findBestSellers(4));
        return "home/index";
    }
}
