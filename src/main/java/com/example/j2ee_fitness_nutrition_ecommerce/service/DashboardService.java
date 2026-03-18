package com.example.j2ee_fitness_nutrition_ecommerce.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.MonthlyStat;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.ProductVariant;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.OrderStatus;

public interface DashboardService {

    long getTotalOrders();

    long getTotalProducts();

    long getTotalUsers();

    BigDecimal getTotalRevenue();

    Map<OrderStatus, Long> getOrderCountsByStatus();

    List<Order> getRecentOrders();

    List<Product> getTopSellingProducts();

    List<ProductVariant> getLowStockVariants();

    List<MonthlyStat> getMonthlyStats();

    long getNewUsersThisMonth();
}
