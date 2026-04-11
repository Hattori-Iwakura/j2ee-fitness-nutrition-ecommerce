package com.example.j2ee_fitness_nutrition_ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProfileUpdateRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phone;

    private String address;
}
