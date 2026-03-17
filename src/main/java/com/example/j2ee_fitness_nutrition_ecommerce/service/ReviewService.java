package com.example.j2ee_fitness_nutrition_ecommerce.service;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Review;

import java.util.List;

public interface ReviewService {
    Review createReview(String userEmail, Long productId, int rating, String comment);
    List<Review> findByProductId(Long productId);
    Double getAverageRating(Long productId);
    long getReviewCount(Long productId);
    boolean hasUserPurchasedProduct(String userEmail, Long productId);
    boolean hasUserReviewedProduct(String userEmail, Long productId);
    List<Review> findAll();
    void deleteById(Long id);
}
