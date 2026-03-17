package com.example.j2ee_fitness_nutrition_ecommerce.service;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.ProductVariant;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private ProductVariantRepository variantRepository;
    @InjectMocks private CartService cartService;

    private MockHttpSession session;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        Product product = Product.builder().id(1L).name("Whey Protein").imageUrl("/img.jpg").build();
        variant = ProductVariant.builder()
                .id(1L).product(product).flavor("Chocolate").weight("2kg")
                .price(new BigDecimal("500000")).stock(10).active(true)
                .build();
    }

    @Test
    void getCart_emptySession_returnsEmptyList() {
        List<CartItem> cart = cartService.getCart(session);

        assertThat(cart).isEmpty();
    }

    @Test
    void addToCart_newItem() {
        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));

        cartService.addToCart(session, 1L, 2);

        List<CartItem> cart = cartService.getCart(session);
        assertThat(cart).hasSize(1);
        assertThat(cart.get(0).getQuantity()).isEqualTo(2);
        assertThat(cart.get(0).getProductName()).isEqualTo("Whey Protein");
        assertThat(cart.get(0).getFlavor()).isEqualTo("Chocolate");
    }

    @Test
    void addToCart_existingItem_incrementsQuantity() {
        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));

        cartService.addToCart(session, 1L, 2);
        cartService.addToCart(session, 1L, 3);

        List<CartItem> cart = cartService.getCart(session);
        assertThat(cart).hasSize(1);
        assertThat(cart.get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void addToCart_variantNotFound_throwsException() {
        when(variantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addToCart(session, 99L, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateQuantity_updatesExistingItem() {
        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));
        cartService.addToCart(session, 1L, 2);

        cartService.updateQuantity(session, 1L, 5);

        assertThat(cartService.getCart(session).get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void updateQuantity_zeroQuantity_removesItem() {
        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));
        cartService.addToCart(session, 1L, 2);

        cartService.updateQuantity(session, 1L, 0);

        assertThat(cartService.getCart(session)).isEmpty();
    }

    @Test
    void removeFromCart_removesItem() {
        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));
        cartService.addToCart(session, 1L, 2);

        cartService.removeFromCart(session, 1L);

        assertThat(cartService.getCart(session)).isEmpty();
    }

    @Test
    void clearCart_removesAllItems() {
        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));
        cartService.addToCart(session, 1L, 2);

        cartService.clearCart(session);

        // After clearCart, getCart creates a fresh empty list
        assertThat(cartService.getCart(session)).isEmpty();
    }

    @Test
    void getCartTotal_calculatesCorrectly() {
        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));
        cartService.addToCart(session, 1L, 3);

        BigDecimal total = cartService.getCartTotal(session);

        assertThat(total).isEqualByComparingTo("1500000"); // 500000 * 3
    }

    @Test
    void getCartCount_returnsTotalQuantity() {
        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));
        cartService.addToCart(session, 1L, 3);

        int count = cartService.getCartCount(session);

        assertThat(count).isEqualTo(3);
    }
}
