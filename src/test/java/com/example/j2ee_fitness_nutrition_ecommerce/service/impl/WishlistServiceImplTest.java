package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.WishlistItem;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.UserRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.WishlistItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceImplTest {

    @Mock private WishlistItemRepository wishlistItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks private WishlistServiceImpl wishlistService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@test.com").fullName("Test User").build();
        product = Product.builder().id(1L).name("Whey Protein").build();
    }

    @Test
    void addToWishlist_success() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(wishlistItemRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        wishlistService.addToWishlist("user@test.com", 1L);

        verify(wishlistItemRepository).save(any(WishlistItem.class));
    }

    @Test
    void addToWishlist_alreadyExists_doesNotDuplicate() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(wishlistItemRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(true);

        wishlistService.addToWishlist("user@test.com", 1L);

        verify(wishlistItemRepository, never()).save(any(WishlistItem.class));
    }

    @Test
    void removeFromWishlist_success() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        wishlistService.removeFromWishlist("user@test.com", 1L);

        verify(wishlistItemRepository).deleteByUserIdAndProductId(1L, 1L);
    }

    @Test
    void isInWishlist_returnsTrue() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(wishlistItemRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(true);

        boolean result = wishlistService.isInWishlist("user@test.com", 1L);

        assertThat(result).isTrue();
    }

    @Test
    void getWishlistedProductIds_returnsIds() {
        WishlistItem item1 = WishlistItem.builder().product(Product.builder().id(1L).build()).build();
        WishlistItem item2 = WishlistItem.builder().product(Product.builder().id(2L).build()).build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(wishlistItemRepository.findByUserIdOrderByAddedAtDesc(1L))
                .thenReturn(List.of(item1, item2));

        Set<Long> result = wishlistService.getWishlistedProductIds("user@test.com");

        assertThat(result).containsExactlyInAnyOrder(1L, 2L);
    }
}
