package com.example.j2ee_fitness_nutrition_ecommerce.service;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.CheckoutRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.OrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    Order createOrder(String userEmail, CheckoutRequest request, List<CartItem> cartItems, String couponCode);
    List<Order> findByUserEmail(String email);
    Optional<Order> findById(Long id);
    Optional<Order> findByOrderCode(String orderCode);
    List<Order> findAll();
    Page<Order> findAll(Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Order updateStatus(Long orderId, OrderStatus status);
    long countByStatus(OrderStatus status);
}
