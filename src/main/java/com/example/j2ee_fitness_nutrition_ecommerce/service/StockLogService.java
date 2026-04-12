package com.example.j2ee_fitness_nutrition_ecommerce.service;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.ProductVariant;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.StockLog;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.StockChangeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockLogService {

    void log(ProductVariant variant, int stockBefore, int stockAfter, StockChangeType changeType);

    Page<StockLog> findAll(Pageable pageable);
}
