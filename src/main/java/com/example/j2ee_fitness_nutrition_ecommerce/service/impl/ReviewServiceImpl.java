package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Review;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.OrderDetailRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ReviewRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.UserRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ReviewService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             UserRepository userRepository,
                             ProductRepository productRepository,
                             OrderDetailRepository orderDetailRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    @Override
    public Review createReview(String userEmail, Long productId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (!hasUserPurchasedProduct(userEmail, productId)) {
            throw new IllegalStateException("You can only review products you have purchased and received");
        }
        if (hasUserReviewedProduct(userEmail, productId)) {
            throw new IllegalStateException("You have already reviewed this product");
        }

        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        return reviewRepository.save(Review.builder()
                .user(user)
                .product(product)
                .rating(rating)
                .comment(comment)
                .build());
    }

    @Override
    public List<Review> findByProductId(Long productId) {
        return reviewRepository.findByProductIdAndDeletedFalseOrderByCreatedAtDesc(productId);
    }

    @Override
    public Double getAverageRating(Long productId) {
        return reviewRepository.findAverageRatingByProductId(productId);
    }

    @Override
    public long getReviewCount(Long productId) {
        return reviewRepository.countByProductId(productId);
    }

    @Override
    public boolean hasUserPurchasedProduct(String userEmail, Long productId) {
        return orderDetailRepository.existsByUserEmailAndProductId(userEmail, productId);
    }

    @Override
    public boolean hasUserReviewedProduct(String userEmail, Long productId) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return reviewRepository.existsByUserIdAndProductIdAndDeletedFalse(user.getId(), productId);
    }

    @Override
    public List<Review> findAll() {
        return reviewRepository.findByDeletedFalse();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        review.setDeleted(true);
        reviewRepository.save(review);
    }
}
