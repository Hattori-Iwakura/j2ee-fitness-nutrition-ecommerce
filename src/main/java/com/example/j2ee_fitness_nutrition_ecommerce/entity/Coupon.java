package com.example.j2ee_fitness_nutrition_ecommerce.entity;

import com.example.j2ee_fitness_nutrition_ecommerce.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 12, scale = 2)
    private BigDecimal minOrderAmount;

    private Integer maxUses;

    @Builder.Default
    private int currentUses = 0;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Builder.Default
    private boolean active = true;
}
