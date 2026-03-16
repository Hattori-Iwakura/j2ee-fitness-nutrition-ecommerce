package com.example.j2ee_fitness_nutrition_ecommerce.service;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.RegisterRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;

import java.util.Optional;

public interface UserService {
    User register(RegisterRequest request);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
