package com.example.j2ee_fitness_nutrition_ecommerce.controller.admin;

import com.example.j2ee_fitness_nutrition_ecommerce.config.SecurityConfig;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.OrderStatus;
import com.example.j2ee_fitness_nutrition_ecommerce.service.EmailService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.OrderService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.PaymentService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
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

@WebMvcTest(AdminOrderController.class)
@Import(SecurityConfig.class)
class AdminOrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private OrderService orderService;
    @MockitoBean private PaymentService paymentService;
    @MockitoBean private EmailService emailService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void list_admin_returnsOrderList() throws Exception {
        when(orderService.findAll(any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/order/list"))
                .andExpect(model().attributeExists("orders", "statuses"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void detail_admin_returnsOrderDetail() throws Exception {
        Order order = Order.builder().id(1L).orderCode("FS-001")
                .status(OrderStatus.PENDING).totalAmount(BigDecimal.valueOf(500000))
                .orderDetails(new ArrayList<>()).build();
        when(orderService.findById(1L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/admin/orders/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/order/detail"))
                .andExpect(model().attributeExists("order", "statuses"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatus_admin_redirectsToDetail() throws Exception {
        Order order = Order.builder().id(1L).orderCode("FS-001")
                .status(OrderStatus.CONFIRMED).build();
        when(orderService.updateStatus(1L, OrderStatus.CONFIRMED)).thenReturn(order);

        mockMvc.perform(post("/admin/orders/1/status")
                        .with(csrf())
                        .param("status", "CONFIRMED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders/1"));

        verify(orderService).updateStatus(1L, OrderStatus.CONFIRMED);
        verify(emailService).sendOrderStatusUpdate(any(), eq(OrderStatus.CONFIRMED));
    }
}
