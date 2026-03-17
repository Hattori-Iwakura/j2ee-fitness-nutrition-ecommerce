package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.ProductFilter;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.service.CategoryService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ProductService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ReviewService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.WishlistService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final WishlistService wishlistService;
    private final ReviewService reviewService;

    public ProductController(ProductService productService, CategoryService categoryService,
                             WishlistService wishlistService, ReviewService reviewService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.wishlistService = wishlistService;
        this.reviewService = reviewService;
    }

    @GetMapping("/products")
    public String listProducts(@ModelAttribute ProductFilter filter,
                               @RequestParam(defaultValue = "0") int page,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {

        Sort sort = resolveSort(filter.getSort());
        PageRequest pageable = PageRequest.of(page, 9, sort);

        Page<Product> products = productService.findWithFilter(filter, pageable);

        model.addAttribute("products", products);
        model.addAttribute("filter", filter);
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("allBrands", productService.findAllBrands());

        // Resolve current category for sidebar highlight
        if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
            categoryService.findActiveBySlug(filter.getCategory())
                    .ifPresent(cat -> model.addAttribute("currentCategory", cat));
        }

        if (userDetails != null) {
            model.addAttribute("wishlistedIds", wishlistService.getWishlistedProductIds(userDetails.getUsername()));
        }

        return "product/list";
    }

    @GetMapping("/products/{slug}")
    public String productDetail(@PathVariable String slug,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        Product product = productService.findActiveBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        model.addAttribute("product", product);

        // Reviews
        model.addAttribute("reviews", reviewService.findByProductId(product.getId()));
        model.addAttribute("avgRating", reviewService.getAverageRating(product.getId()));
        model.addAttribute("reviewCount", reviewService.getReviewCount(product.getId()));

        // Auth-dependent attributes
        if (userDetails != null) {
            String email = userDetails.getUsername();
            model.addAttribute("isWishlisted", wishlistService.isInWishlist(email, product.getId()));
            model.addAttribute("canReview", reviewService.hasUserPurchasedProduct(email, product.getId())
                    && !reviewService.hasUserReviewedProduct(email, product.getId()));
            model.addAttribute("hasReviewed", reviewService.hasUserReviewedProduct(email, product.getId()));
        }

        // Recommendations: co-purchased first, fall back to same-category
        var coPurchased = productService.findCoPurchasedProducts(product.getId());
        if (!coPurchased.isEmpty()) {
            model.addAttribute("recommendedProducts", coPurchased);
            model.addAttribute("recommendationTitle", "Customers Also Bought");
        } else {
            model.addAttribute("recommendedProducts", productService.findRelatedProducts(product.getId(), product.getCategory().getId()));
            model.addAttribute("recommendationTitle", "Related Products");
        }

        return "product/detail";
    }

    private Sort resolveSort(String sortParam) {
        if (sortParam == null) {
            return Sort.by("createdAt").descending();
        }
        return switch (sortParam) {
            // Price sorting is handled inside ProductSpecification via subquery
            case "price-asc", "price-desc" -> Sort.unsorted();
            case "name-asc" -> Sort.by("name").ascending();
            case "name-desc" -> Sort.by("name").descending();
            default -> Sort.by("createdAt").descending();
        };
    }
}
