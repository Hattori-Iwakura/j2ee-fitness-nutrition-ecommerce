package com.example.j2ee_fitness_nutrition_ecommerce.config;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.*;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.DiscountType;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.Role;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.CategoryRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.CouponRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    @Order(1)
    CommandLineRunner initData(UserRepository userRepository,
                               CategoryRepository categoryRepository,
                               ProductRepository productRepository,
                               CouponRepository couponRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) return;

            // Create admin user
            userRepository.save(User.builder()
                    .fullName("Admin")
                    .email("admin@fitshop.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build());

            // Create test user
            userRepository.save(User.builder()
                    .fullName("Test User")
                    .email("user@fitshop.com")
                    .password(passwordEncoder.encode("user123"))
                    .role(Role.USER)
                    .build());

            // Categories
            Category wheyProtein = categoryRepository.save(Category.builder()
                    .name("Whey Protein").slug("whey-protein")
                    .description("High-quality whey protein powders for muscle recovery and growth").build());

            Category massGainer = categoryRepository.save(Category.builder()
                    .name("Mass Gainer").slug("mass-gainer")
                    .description("Calorie-dense supplements for weight and muscle gain").build());

            Category preWorkout = categoryRepository.save(Category.builder()
                    .name("Pre-Workout").slug("pre-workout")
                    .description("Energy and performance boosting supplements").build());

            // Sample products with variants
            Product p1 = Product.builder()
                    .name("Optimum Nutrition Gold Standard Whey")
                    .slug("on-gold-standard-whey")
                    .description("The world's best-selling whey protein powder")
                    .brand("Optimum Nutrition")
                    .category(wheyProtein)
                    .build();
            p1.getVariants().add(ProductVariant.builder().flavor("Chocolate").weight("2 lbs").price(new BigDecimal("750000")).stock(50).sku("ON-WH-CHO-2").product(p1).build());
            p1.getVariants().add(ProductVariant.builder().flavor("Vanilla").weight("2 lbs").price(new BigDecimal("750000")).stock(30).sku("ON-WH-VAN-2").product(p1).build());
            p1.getVariants().add(ProductVariant.builder().flavor("Chocolate").weight("5 lbs").price(new BigDecimal("1500000")).stock(20).sku("ON-WH-CHO-5").product(p1).build());
            productRepository.save(p1);

            Product p2 = Product.builder()
                    .name("Serious Mass")
                    .slug("serious-mass")
                    .description("High-calorie mass gainer for hard gainers")
                    .brand("Optimum Nutrition")
                    .category(massGainer)
                    .build();
            p2.getVariants().add(ProductVariant.builder().flavor("Chocolate").weight("6 lbs").price(new BigDecimal("900000")).stock(25).sku("SM-CHO-6").product(p2).build());
            p2.getVariants().add(ProductVariant.builder().flavor("Banana").weight("6 lbs").price(new BigDecimal("900000")).stock(15).sku("SM-BAN-6").product(p2).build());
            productRepository.save(p2);

            Product p3 = Product.builder()
                    .name("C4 Original Pre-Workout")
                    .slug("c4-original")
                    .description("America's #1 selling pre-workout brand")
                    .brand("Cellucor")
                    .category(preWorkout)
                    .build();
            p3.getVariants().add(ProductVariant.builder().flavor("Fruit Punch").weight("30 servings").price(new BigDecimal("550000")).stock(40).sku("C4-FP-30").product(p3).build());
            p3.getVariants().add(ProductVariant.builder().flavor("Blue Raspberry").weight("30 servings").price(new BigDecimal("550000")).stock(35).sku("C4-BR-30").product(p3).build());
            productRepository.save(p3);

            // Sample coupons
            couponRepository.save(Coupon.builder()
                    .code("WELCOME10")
                    .discountType(DiscountType.PERCENTAGE)
                    .discountValue(new BigDecimal("10"))
                    .minOrderAmount(new BigDecimal("500000"))
                    .maxUses(100)
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now().plusMonths(3))
                    .build());

            couponRepository.save(Coupon.builder()
                    .code("FLAT50K")
                    .discountType(DiscountType.FIXED)
                    .discountValue(new BigDecimal("50000"))
                    .minOrderAmount(new BigDecimal("300000"))
                    .maxUses(50)
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now().plusMonths(1))
                    .build());
        };
    }
}
