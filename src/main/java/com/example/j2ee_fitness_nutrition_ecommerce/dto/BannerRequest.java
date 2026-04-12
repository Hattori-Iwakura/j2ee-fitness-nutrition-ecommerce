package com.example.j2ee_fitness_nutrition_ecommerce.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BannerRequest {

    private Long id;

    private String imageUrl;

    /** Optional; empty means no navigation (stored as "#"). */
    private String linkUrl;

    private boolean active;

    @Min(0)
    private int sortOrder;
}
