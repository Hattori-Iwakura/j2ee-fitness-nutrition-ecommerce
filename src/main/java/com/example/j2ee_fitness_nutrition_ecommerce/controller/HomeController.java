package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.repository.CategoryRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.service.BannerService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final CategoryRepository categoryRepository;
    private final ProductService productService;
    private final BannerService bannerService;

    public HomeController(CategoryRepository categoryRepository,
                          ProductService productService,
                          BannerService bannerService) {
        this.categoryRepository = categoryRepository;
        this.productService = productService;
        this.bannerService = bannerService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryRepository.findByActiveTrue());
        model.addAttribute("featuredProducts", productService.findNewArrivals(8));
        model.addAttribute("bestSellers", productService.findBestSellers(4));
        model.addAttribute("banners", bannerService.findActiveForHome());
        return "home/index";
    }
}
