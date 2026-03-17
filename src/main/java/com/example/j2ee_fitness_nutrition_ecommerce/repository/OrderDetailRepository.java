package com.example.j2ee_fitness_nutrition_ecommerce.repository;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Query("SELECT COUNT(od) > 0 FROM OrderDetail od WHERE od.order.user.email = :email AND od.variant.product.id = :productId AND od.order.status = com.example.j2ee_fitness_nutrition_ecommerce.enums.OrderStatus.DELIVERED")
    boolean existsByUserEmailAndProductId(@Param("email") String email, @Param("productId") Long productId);
}
