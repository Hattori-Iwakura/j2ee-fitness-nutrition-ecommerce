package com.example.j2ee_fitness_nutrition_ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class MonthlyStat {
    private String month;
    private BigDecimal revenue;
    private long orderCount;
}
