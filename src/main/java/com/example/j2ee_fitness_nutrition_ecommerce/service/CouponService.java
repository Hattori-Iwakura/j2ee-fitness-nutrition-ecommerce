package com.example.j2ee_fitness_nutrition_ecommerce.service;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Coupon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CouponService {
    Coupon validate(String code, BigDecimal orderTotal);
    BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderTotal);
    void incrementUsage(Coupon coupon);
    List<Coupon> findAll();
    Optional<Coupon> findById(Long id);
    Coupon save(Coupon coupon);
    void deleteById(Long id);
}
