package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.config.SecurityConfig;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Category;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.service.CategoryService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ProductService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ReviewService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.WishlistService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ProductService productService;
    @MockitoBean private CategoryService categoryService;
    @MockitoBean private WishlistService wishlistService;
    @MockitoBean private ReviewService reviewService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @Test
    void listProducts_noParams_returnsAllActive() throws Exception {
        Product p = Product.builder().id(1L).name("Whey").active(true).build();
        when(productService.findWithFilter(any(), any())).thenReturn(new PageImpl<>(List.of(p)));
        when(categoryService.findAllActive()).thenReturn(List.of());
        when(productService.findAllBrands()).thenReturn(List.of());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/list"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    void listProducts_withKeyword_searchesByName() throws Exception {
        when(productService.findWithFilter(any(), any())).thenReturn(new PageImpl<>(List.of()));
        when(categoryService.findAllActive()).thenReturn(List.of());
        when(productService.findAllBrands()).thenReturn(List.of());

        mockMvc.perform(get("/products").param("keyword", "whey"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/list"));

        verify(productService).findWithFilter(any(), any());
    }

    @Test
    void listProducts_withCategory_filtersByCategory() throws Exception {
        when(productService.findWithFilter(any(), any())).thenReturn(new PageImpl<>(List.of()));
        when(categoryService.findAllActive()).thenReturn(List.of());
        when(categoryService.findActiveBySlug("protein")).thenReturn(
                Optional.of(Category.builder().id(1L).slug("protein").build()));
        when(productService.findAllBrands()).thenReturn(List.of());

        mockMvc.perform(get("/products").param("category", "protein"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/list"));
    }

    @Test
    void productDetail_validSlug_returnsDetail() throws Exception {
        Category cat = Category.builder().id(1L).name("Protein").build();
        Product product = Product.builder().id(1L).slug("whey-gold").name("Whey Gold")
                .active(true).category(cat).build();
        when(productService.findActiveBySlug("whey-gold")).thenReturn(Optional.of(product));
        when(reviewService.findByProductId(1L)).thenReturn(List.of());
        when(reviewService.getAverageRating(1L)).thenReturn(4.5);
        when(reviewService.getReviewCount(1L)).thenReturn(10L);
        when(productService.findCoPurchasedProducts(1L)).thenReturn(List.of());
        when(productService.findRelatedProducts(1L, 1L)).thenReturn(List.of());

        mockMvc.perform(get("/products/whey-gold"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/detail"))
                .andExpect(model().attributeExists("product"));
    }
}
