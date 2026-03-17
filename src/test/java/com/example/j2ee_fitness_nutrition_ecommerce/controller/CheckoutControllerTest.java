package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.config.SecurityConfig;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.OrderStatus;
import com.example.j2ee_fitness_nutrition_ecommerce.service.*;
import com.example.j2ee_fitness_nutrition_ecommerce.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CheckoutController.class)
@Import(SecurityConfig.class)
class CheckoutControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private CartService cartService;
    @MockitoBean private OrderService orderService;
    @MockitoBean private UserService userService;
    @MockitoBean private CouponService couponService;
    @MockitoBean private PaymentService paymentService;
    @MockitoBean private ProductService productService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "user@test.com")
    void getCheckout_emptyCart_redirectsToCart() throws Exception {
        when(cartService.getCart(any())).thenReturn(List.of());

        mockMvc.perform(get("/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void getCheckout_withItems_returnsCheckoutView() throws Exception {
        CartItem item = new CartItem(1L, "Whey", "Chocolate", "2kg", BigDecimal.valueOf(500000), 2, "/img.jpg");
        when(cartService.getCart(any())).thenReturn(List.of(item));
        when(cartService.getCartTotal(any())).thenReturn(BigDecimal.valueOf(1000000));
        when(userService.findByEmail("user@test.com"))
                .thenReturn(Optional.of(User.builder().fullName("Test").phone("012").build()));

        mockMvc.perform(get("/checkout"))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout/index"))
                .andExpect(model().attributeExists("checkoutRequest", "cartItems", "cartTotal"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void postCheckout_valid_redirectsToSuccess() throws Exception {
        CartItem item = new CartItem(1L, "Whey", "Chocolate", "2kg", BigDecimal.valueOf(500000), 2, "/img.jpg");
        when(cartService.getCart(any())).thenReturn(List.of(item));
        Order order = Order.builder().id(1L).orderCode("FS-001").status(OrderStatus.PENDING)
                .orderDetails(new ArrayList<>()).build();
        when(orderService.createOrder(anyString(), any(), any(), any())).thenReturn(order);

        mockMvc.perform(post("/checkout")
                        .with(csrf())
                        .param("fullName", "John Doe")
                        .param("phone", "0123456789")
                        .param("address", "123 Main St")
                        .param("paymentMethod", "COD"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout/success?code=FS-001"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void postCheckout_validationErrors_returnsForm() throws Exception {
        CartItem item = new CartItem(1L, "Whey", "Chocolate", "2kg", BigDecimal.valueOf(500000), 2, "/img.jpg");
        when(cartService.getCart(any())).thenReturn(List.of(item));
        when(cartService.getCartTotal(any())).thenReturn(BigDecimal.valueOf(1000000));

        mockMvc.perform(post("/checkout")
                        .with(csrf())
                        .param("fullName", "")
                        .param("phone", "")
                        .param("address", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout/index"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void postCheckout_stockError_returnsFormWithError() throws Exception {
        CartItem item = new CartItem(1L, "Whey", "Chocolate", "2kg", BigDecimal.valueOf(500000), 2, "/img.jpg");
        when(cartService.getCart(any())).thenReturn(List.of(item));
        when(cartService.getCartTotal(any())).thenReturn(BigDecimal.valueOf(1000000));
        when(orderService.createOrder(anyString(), any(), any(), any()))
                .thenThrow(new IllegalStateException("Insufficient stock for Whey"));

        mockMvc.perform(post("/checkout")
                        .with(csrf())
                        .param("fullName", "John Doe")
                        .param("phone", "0123456789")
                        .param("address", "123 Main St")
                        .param("paymentMethod", "COD"))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout/index"))
                .andExpect(model().attributeExists("error"));
    }
}
