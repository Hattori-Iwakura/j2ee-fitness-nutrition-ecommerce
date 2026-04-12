package com.example.j2ee_fitness_nutrition_ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactForm {

    @NotBlank
    @Size(max = 200)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 320)
    private String email;

    @Size(max = 50)
    private String phone;

    @NotBlank
    @Size(max = 4000)
    private String message;
}
