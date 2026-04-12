package com.example.j2ee_fitness_nutrition_ecommerce.repository;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.StockLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockLogRepository extends JpaRepository<StockLog, Long> {

    @EntityGraph(attributePaths = {"variant", "variant.product"})
    Page<StockLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
