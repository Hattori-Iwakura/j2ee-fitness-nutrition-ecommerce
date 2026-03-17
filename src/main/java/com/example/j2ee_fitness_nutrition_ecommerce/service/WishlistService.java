package com.example.j2ee_fitness_nutrition_ecommerce.service;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.WishlistItem;

import java.util.List;
import java.util.Set;

public interface WishlistService {
    void addToWishlist(String userEmail, Long productId);
    void removeFromWishlist(String userEmail, Long productId);
    List<WishlistItem> getWishlist(String userEmail);
    boolean isInWishlist(String userEmail, Long productId);
    Set<Long> getWishlistedProductIds(String userEmail);
}
