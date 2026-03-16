package com.example.j2ee_fitness_nutrition_ecommerce.service;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.ProductVariant;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductVariantRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private static final String CART_SESSION_KEY = "cart";
    private final ProductVariantRepository variantRepository;

    public CartService(ProductVariantRepository variantRepository) {
        this.variantRepository = variantRepository;
    }

    @SuppressWarnings("unchecked")
    public List<CartItem> getCart(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    public void addToCart(HttpSession session, Long variantId, int quantity) {
        List<CartItem> cart = getCart(session);
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));

        for (CartItem item : cart) {
            if (item.getVariantId().equals(variantId)) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }

        CartItem item = new CartItem(
                variant.getId(),
                variant.getProduct().getName(),
                variant.getFlavor(),
                variant.getWeight(),
                variant.getPrice(),
                quantity,
                variant.getProduct().getImageUrl()
        );
        cart.add(item);
    }

    public void updateQuantity(HttpSession session, Long variantId, int quantity) {
        List<CartItem> cart = getCart(session);
        for (CartItem item : cart) {
            if (item.getVariantId().equals(variantId)) {
                if (quantity <= 0) {
                    cart.remove(item);
                } else {
                    item.setQuantity(quantity);
                }
                return;
            }
        }
    }

    public void removeFromCart(HttpSession session, Long variantId) {
        List<CartItem> cart = getCart(session);
        cart.removeIf(item -> item.getVariantId().equals(variantId));
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    public BigDecimal getCartTotal(HttpSession session) {
        return getCart(session).stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getCartCount(HttpSession session) {
        return getCart(session).stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}
