package com.example.j2ee_fitness_nutrition_ecommerce.dto;

import com.example.j2ee_fitness_nutrition_ecommerce.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
public class CouponRequest {

    private Long id;

    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 50, message = "Code must be between 3 and 50 characters")
    private String code;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0", message = "Minimum order amount cannot be negative")
    private BigDecimal minOrderAmount;

    @Min(value = 1, message = "Max uses must be at least 1")
    private Integer maxUses;

    private int currentUses;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private boolean active = true;
}
