package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.config.SecurityConfig;
import com.example.j2ee_fitness_nutrition_ecommerce.service.CartService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@Import(SecurityConfig.class)
class CartControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private CartService cartService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void viewCart_authenticated_returnsCartView() throws Exception {
        when(cartService.getCart(any())).thenReturn(List.of());
        when(cartService.getCartTotal(any())).thenReturn(BigDecimal.ZERO);

        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/index"));
    }

    @Test
    @WithMockUser
    void addToCart_authenticated_redirectsToCart() throws Exception {
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .param("variantId", "1")
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).addToCart(any(), eq(1L), eq(2));
    }

    @Test
    @WithMockUser
    void updateQuantity_authenticated_redirectsToCart() throws Exception {
        mockMvc.perform(post("/cart/update")
                        .with(csrf())
                        .param("variantId", "1")
                        .param("quantity", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).updateQuantity(any(), eq(1L), eq(5));
    }

    @Test
    void viewCart_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().is3xxRedirection());
    }
}
