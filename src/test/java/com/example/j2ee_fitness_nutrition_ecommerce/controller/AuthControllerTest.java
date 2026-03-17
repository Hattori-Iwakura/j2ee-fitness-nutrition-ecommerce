package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.config.SecurityConfig;
import com.example.j2ee_fitness_nutrition_ecommerce.service.UserService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @Test
    void getLogin_returnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void getRegister_returnsRegisterViewWithEmptyForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void postRegister_validData_redirectsToLogin() throws Exception {
        when(userService.existsByEmail(anyString())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "John Doe")
                        .param("email", "john@test.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .param("phone", "0123456789"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService).register(any());
    }

    @Test
    void postRegister_validationErrors_returnsForm() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "")
                        .param("email", "not-an-email")
                        .param("password", "abc")
                        .param("confirmPassword", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));

        verify(userService, never()).register(any());
    }

    @Test
    void postRegister_duplicateEmail_returnsFormWithError() throws Exception {
        when(userService.existsByEmail("taken@test.com")).thenReturn(true);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "John Doe")
                        .param("email", "taken@test.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));

        verify(userService, never()).register(any());
    }
}
