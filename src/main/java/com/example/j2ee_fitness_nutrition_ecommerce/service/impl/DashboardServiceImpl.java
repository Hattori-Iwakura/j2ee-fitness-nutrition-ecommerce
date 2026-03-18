package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.MonthlyStat;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.ProductVariant;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.OrderStatus;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.OrderRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductVariantRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.service.DashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    public DashboardServiceImpl(OrderRepository orderRepository,
                                ProductRepository productRepository,
                                ProductVariantRepository productVariantRepository,
                                UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.userRepository = userRepository;
    }

    @Override
    public long getTotalOrders() {
        return orderRepository.count();
    }

    @Override
    public long getTotalProducts() {
        return productRepository.count();
    }

    @Override
    public long getTotalUsers() {
        return userRepository.count();
    }

    @Override
    public BigDecimal getTotalRevenue() {
        return orderRepository.sumTotalRevenue();
    }

    @Override
    public Map<OrderStatus, Long> getOrderCountsByStatus() {
        Map<OrderStatus, Long> counts = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            counts.put(status, orderRepository.countByStatus(status));
        }
        return counts;
    }

    @Override
    public List<Order> getRecentOrders() {
        return orderRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Override
    public List<Product> getTopSellingProducts() {
        return productRepository.findBestSellers(PageRequest.of(0, 5));
    }

    @Override
    public List<ProductVariant> getLowStockVariants() {
        return productVariantRepository.findByActiveTrueAndStockLessThanEqualOrderByStockAsc(LOW_STOCK_THRESHOLD);
    }

    @Override
    public List<MonthlyStat> getMonthlyStats() {
        List<MonthlyStat> stats = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            LocalDateTime from = monthStart.atStartOfDay();
            LocalDateTime to = monthStart.plusMonths(1).atStartOfDay();

            BigDecimal revenue = orderRepository.sumRevenueByPeriod(from, to);
            long orderCount = orderRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(from, to);

            stats.add(new MonthlyStat(monthStart.format(formatter), revenue, orderCount));
        }

        return stats;
    }

    @Override
    public long getNewUsersThisMonth() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return userRepository.countByCreatedAtAfter(startOfMonth);
    }
}
