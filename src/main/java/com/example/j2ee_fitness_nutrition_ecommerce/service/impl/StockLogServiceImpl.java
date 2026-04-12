package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.ProductVariant;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.StockLog;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.StockChangeType;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.StockLogRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.service.StockLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockLogServiceImpl implements StockLogService {

    private final StockLogRepository stockLogRepository;

    public StockLogServiceImpl(StockLogRepository stockLogRepository) {
        this.stockLogRepository = stockLogRepository;
    }

    @Override
    @Transactional
    public void log(ProductVariant variant, int stockBefore, int stockAfter, StockChangeType changeType) {
        if (stockBefore == stockAfter) {
            return;
        }
        StockLog entry = StockLog.builder()
                .variant(variant)
                .stockBefore(stockBefore)
                .stockAfter(stockAfter)
                .changeType(changeType)
                .build();
        stockLogRepository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockLog> findAll(Pageable pageable) {
        return stockLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
