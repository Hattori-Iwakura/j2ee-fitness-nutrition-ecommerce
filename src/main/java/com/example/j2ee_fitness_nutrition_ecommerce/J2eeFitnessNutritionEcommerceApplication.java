package com.example.j2ee_fitness_nutrition_ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class J2eeFitnessNutritionEcommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(J2eeFitnessNutritionEcommerceApplication.class, args);
	}

}
