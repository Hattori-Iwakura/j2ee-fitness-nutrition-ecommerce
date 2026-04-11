package com.example.j2ee_fitness_nutrition_ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {

    @NotBlank(message = "Message cannot be empty")
    @Size(max = 1000, message = "Message too long")
    private String message;
}
