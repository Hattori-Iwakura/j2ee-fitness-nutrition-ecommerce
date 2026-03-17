package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.WishlistItem;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.UserRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.WishlistItemRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.service.WishlistService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WishlistServiceImpl implements WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public WishlistServiceImpl(WishlistItemRepository wishlistItemRepository,
                               UserRepository userRepository,
                               ProductRepository productRepository) {
        this.wishlistItemRepository = wishlistItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void addToWishlist(String userEmail, Long productId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (wishlistItemRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            return;
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        wishlistItemRepository.save(WishlistItem.builder()
                .user(user)
                .product(product)
                .build());
    }

    @Override
    @Transactional
    public void removeFromWishlist(String userEmail, Long productId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        wishlistItemRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }

    @Override
    public List<WishlistItem> getWishlist(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return wishlistItemRepository.findByUserIdOrderByAddedAtDesc(user.getId());
    }

    @Override
    public boolean isInWishlist(String userEmail, Long productId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return wishlistItemRepository.existsByUserIdAndProductId(user.getId(), productId);
    }

    @Override
    public Set<Long> getWishlistedProductIds(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return wishlistItemRepository.findByUserIdOrderByAddedAtDesc(user.getId()).stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toSet());
    }
}
