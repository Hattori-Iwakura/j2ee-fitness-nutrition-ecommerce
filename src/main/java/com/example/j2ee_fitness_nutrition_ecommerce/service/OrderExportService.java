package com.example.j2ee_fitness_nutrition_ecommerce.service;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;

import java.util.List;

public interface OrderExportService {
    byte[] exportOrdersToCsv(List<Order> orders);
}
