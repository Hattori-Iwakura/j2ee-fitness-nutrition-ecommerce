package com.example.j2ee_fitness_nutrition_ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CheckoutRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;

    private String note;

    private String couponCode;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
}
