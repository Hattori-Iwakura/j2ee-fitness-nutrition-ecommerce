package com.example.j2ee_fitness_nutrition_ecommerce.service;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.OrderStatus;

public interface EmailService {
    void sendOrderConfirmation(Order order);
    void sendOrderStatusUpdate(Order order, OrderStatus newStatus);
    void sendWelcomeEmail(User user);
}
