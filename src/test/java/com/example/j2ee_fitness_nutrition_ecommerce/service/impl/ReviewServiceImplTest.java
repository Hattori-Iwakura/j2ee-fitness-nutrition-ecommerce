package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Review;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.OrderDetailRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ReviewRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderDetailRepository orderDetailRepository;

    @InjectMocks private ReviewServiceImpl reviewService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@test.com").fullName("Test User").build();
        product = Product.builder().id(1L).name("Whey Protein").slug("whey-protein").build();
    }

    @Test
    void createReview_success() {
        when(orderDetailRepository.existsByUserEmailAndProductId("user@test.com", 1L)).thenReturn(true);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(reviewRepository.existsByUserIdAndProductIdAndDeletedFalse(1L, 1L)).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        Review result = reviewService.createReview("user@test.com", 1L, 5, "Great product!");

        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getComment()).isEqualTo("Great product!");
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_invalidRating_throwsException() {
        assertThatThrownBy(() -> reviewService.createReview("user@test.com", 1L, 0, "Bad"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rating must be between 1 and 5");

        assertThatThrownBy(() -> reviewService.createReview("user@test.com", 1L, 6, "Too high"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rating must be between 1 and 5");
    }

    @Test
    void createReview_notPurchased_throwsException() {
        when(orderDetailRepository.existsByUserEmailAndProductId("user@test.com", 1L)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.createReview("user@test.com", 1L, 5, "Nice"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("purchased and received");
    }

    @Test
    void createReview_alreadyReviewed_throwsException() {
        when(orderDetailRepository.existsByUserEmailAndProductId("user@test.com", 1L)).thenReturn(true);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(reviewRepository.existsByUserIdAndProductIdAndDeletedFalse(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview("user@test.com", 1L, 4, "Again"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already reviewed");
    }

    @Test
    void findByProductId_returnsNonDeletedReviews() {
        Review review = Review.builder().id(1L).rating(5).deleted(false).build();
        when(reviewRepository.findByProductIdAndDeletedFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(review));

        List<Review> result = reviewService.findByProductId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void deleteById_softDelete() {
        Review review = Review.builder().id(1L).rating(5).deleted(false).build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        reviewService.deleteById(1L);

        assertThat(review.isDeleted()).isTrue();
        verify(reviewRepository).save(review);
        verify(reviewRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAverageRating_returnsValue() {
        when(reviewRepository.findAverageRatingByProductId(1L)).thenReturn(4.5);

        Double avg = reviewService.getAverageRating(1L);

        assertThat(avg).isEqualTo(4.5);
    }

    @Test
    void getReviewCount_returnsCount() {
        when(reviewRepository.countByProductId(1L)).thenReturn(10L);

        long count = reviewService.getReviewCount(1L);

        assertThat(count).isEqualTo(10);
    }
}
