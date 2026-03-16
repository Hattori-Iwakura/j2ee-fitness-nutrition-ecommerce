package com.example.j2ee_fitness_nutrition_ecommerce.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CartItem implements Serializable {
    private Long variantId;
    private String productName;
    private String flavor;
    private String weight;
    private BigDecimal price;
    private int quantity;
    private String imageUrl;

    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
