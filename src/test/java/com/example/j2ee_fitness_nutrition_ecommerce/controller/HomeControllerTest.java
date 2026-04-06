package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Category;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.CategoryRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ProductService;
import com.example.j2ee_fitness_nutrition_ecommerce.config.SecurityConfig;
import com.example.j2ee_fitness_nutrition_ecommerce.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)
class HomeControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private CategoryRepository categoryRepository;
    @MockitoBean private ProductService productService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @Test
    void home_returnsIndexWithCategories() throws Exception {
        Category cat = Category.builder().id(1L).name("Protein").active(true).build();
        when(categoryRepository.findByActiveTrue()).thenReturn(List.of(cat));
        when(productService.findBestSellers(anyInt())).thenReturn(List.of());
        when(productService.findNewArrivals(anyInt())).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/index"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("featuredProducts"))
                .andExpect(model().attributeExists("bestSellers"));
    }
}
