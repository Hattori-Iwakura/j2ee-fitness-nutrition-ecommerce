package com.example.j2ee_fitness_nutrition_ecommerce.controller.admin;

import com.example.j2ee_fitness_nutrition_ecommerce.config.SecurityConfig;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Category;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.ProductVariant;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductVariantRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.service.CategoryService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.FileStorageService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ProductService;
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

@WebMvcTest(AdminProductController.class)
@Import(SecurityConfig.class)
class AdminProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ProductService productService;
    @MockitoBean private CategoryService categoryService;
    @MockitoBean private ProductVariantRepository variantRepository;
    @MockitoBean private FileStorageService fileStorageService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void list_admin_returnsProductList() throws Exception {
        when(productService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product/list"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createForm_admin_returnsFormWithCategories() throws Exception {
        when(categoryService.findAllActive()).thenReturn(List.of());

        mockMvc.perform(get("/admin/products/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product/form"))
                .andExpect(model().attributeExists("product", "categories"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void save_admin_redirectsToList() throws Exception {
        Category cat = Category.builder().id(1L).name("Protein").build();
        when(categoryService.findById(1L)).thenReturn(Optional.of(cat));
        when(productService.save(any(Product.class))).thenReturn(
                Product.builder().id(1L).name("New").build());

        mockMvc.perform(post("/admin/products/save")
                        .with(csrf())
                        .param("name", "New Product")
                        .param("slug", "new-product")
                        .param("categoryId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void variants_admin_returnsVariantsView() throws Exception {
        Product product = Product.builder().id(1L).name("Whey")
                .variants(new ArrayList<>()).build();
        when(productService.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/admin/products/1/variants"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product/variants"))
                .andExpect(model().attributeExists("product", "variant"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveVariant_admin_redirectsToVariants() throws Exception {
        Product product = Product.builder().id(1L).name("Whey").build();
        when(productService.findById(1L)).thenReturn(Optional.of(product));
        when(variantRepository.save(any(ProductVariant.class))).thenReturn(
                ProductVariant.builder().id(1L).build());

        mockMvc.perform(post("/admin/products/1/variants/save")
                        .with(csrf())
                        .param("flavor", "Chocolate")
                        .param("weight", "2kg")
                        .param("price", "500000")
                        .param("stock", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products/1/variants"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteVariant_admin_redirectsToVariants() throws Exception {
        ProductVariant variant = ProductVariant.builder().id(2L).active(true).build();
        when(variantRepository.findById(2L)).thenReturn(Optional.of(variant));
        when(variantRepository.save(any())).thenReturn(variant);

        mockMvc.perform(post("/admin/products/1/variants/delete/2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products/1/variants"));
    }
}
