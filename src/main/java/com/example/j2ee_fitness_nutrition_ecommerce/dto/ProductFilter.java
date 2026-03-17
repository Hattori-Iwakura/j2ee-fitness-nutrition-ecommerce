package com.example.j2ee_fitness_nutrition_ecommerce.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductFilter {
    private String keyword;
    private String category;       // category slug
    private List<String> brands;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private boolean inStockOnly;
    private String sort;           // newest, price-asc, price-desc, name-asc, name-desc
}
