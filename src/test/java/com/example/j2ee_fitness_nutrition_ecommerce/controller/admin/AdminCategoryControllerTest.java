package com.example.j2ee_fitness_nutrition_ecommerce.controller.admin;

import com.example.j2ee_fitness_nutrition_ecommerce.config.SecurityConfig;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Category;
import com.example.j2ee_fitness_nutrition_ecommerce.service.CategoryService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.FileStorageService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCategoryController.class)
@Import(SecurityConfig.class)
class AdminCategoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private CategoryService categoryService;
    @MockitoBean private FileStorageService fileStorageService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void list_admin_returnsCategoryList() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of(
                Category.builder().id(1L).name("Protein").build()));

        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/category/list"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createForm_admin_returnsForm() throws Exception {
        mockMvc.perform(get("/admin/categories/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/category/form"))
                .andExpect(model().attributeExists("category"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void save_admin_redirectsToList() throws Exception {
        when(categoryService.save(any(Category.class))).thenReturn(
                Category.builder().id(1L).name("New").build());

        mockMvc.perform(post("/admin/categories/save")
                        .with(csrf())
                        .param("name", "New Category")
                        .param("slug", "new-category"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));

        verify(categoryService).save(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editForm_admin_returnsFormWithCategory() throws Exception {
        Category cat = Category.builder().id(1L).name("Protein").slug("protein").build();
        when(categoryService.findById(1L)).thenReturn(Optional.of(cat));

        mockMvc.perform(get("/admin/categories/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/category/form"))
                .andExpect(model().attributeExists("category"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_admin_redirectsToList() throws Exception {
        mockMvc.perform(post("/admin/categories/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));

        verify(categoryService).deleteById(1L);
    }
}
