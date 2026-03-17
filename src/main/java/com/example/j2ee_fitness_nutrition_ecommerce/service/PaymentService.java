package com.example.j2ee_fitness_nutrition_ecommerce.service;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Payment;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.PaymentMethod;

import java.util.Optional;

public interface PaymentService {
    Payment createPayment(Order order, PaymentMethod method);
    Payment confirmPayment(Long paymentId);
    Payment failPayment(Long paymentId);
    Optional<Payment> findByOrderId(Long orderId);
}
