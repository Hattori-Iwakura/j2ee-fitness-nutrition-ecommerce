package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.service.ProductService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ReviewService;
import com.example.j2ee_fitness_nutrition_ecommerce.util.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReviewController {

    private final ReviewService reviewService;
    private final ProductService productService;

    public ReviewController(ReviewService reviewService, ProductService productService) {
        this.reviewService = reviewService;
        this.productService = productService;
    }

    @PostMapping("/products/{slug}/reviews")
    public String submitReview(@PathVariable String slug,
                               @RequestParam int rating,
                               @RequestParam(required = false) String comment,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        String email = SecurityUtils.requireUserEmail(authentication);
        var product = productService.findActiveBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        try {
            reviewService.createReview(email, product.getId(), rating, comment);
            redirectAttributes.addFlashAttribute("success", "Review submitted successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + slug;
    }
}
