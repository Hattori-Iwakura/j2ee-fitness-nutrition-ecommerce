package com.example.j2ee_fitness_nutrition_ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class ProductVariantRequest {

    private Long id;

    @NotBlank(message = "Flavor is required")
    @Size(max = 100, message = "Flavor must not exceed 100 characters")
    private String flavor;

    @NotBlank(message = "Weight is required")
    @Size(max = 50, message = "Weight must not exceed 50 characters")
    private String weight;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stock;

    @Size(max = 50, message = "SKU must not exceed 50 characters")
    private String sku;
}
