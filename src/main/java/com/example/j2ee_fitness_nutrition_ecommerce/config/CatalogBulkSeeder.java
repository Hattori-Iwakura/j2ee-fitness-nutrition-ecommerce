package com.example.j2ee_fitness_nutrition_ecommerce.config;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Category;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.ProductVariant;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.CategoryRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the catalog up to {@link #TARGET_PRODUCT_COUNT} products, split across whey, mass gainer,
 * and pre-workout. Idempotent: skips when the DB already has enough products.
 */
@Configuration
public class CatalogBulkSeeder {

    static final int TARGET_PRODUCT_COUNT = 200;

    private static final String[][] WHEY_LINES = {
            {"Optimum Nutrition", "Gold Standard 100% Whey"},
            {"Dymatize", "ISO100 Hydrolyzed"},
            {"MuscleTech", "Nitro-Tech Whey Gold"},
            {"Rule 1", "R1 Whey Blend"},
            {"BSN", "Syntha-6 Edge"},
            {"MyProtein", "Impact Whey Isolate"},
            {"Ghost", "Whey Protein"},
            {"Isopure", "Zero Carb Whey Isolate"},
            {"Nutrex", "IsoFit 100% Whey Isolate"},
            {"NutraBio", "Classic Whey"},
            {"AllMax", "AllWhey Gold"},
            {"Universal Nutrition", "Animal Whey"},
            {"EVLution Nutrition", "Stacked Protein"},
            {"GAT Sport", "Whey Matrix"},
            {"Kaged Muscle", "Whey Protein Isolate"},
            {"Legion Athletics", "Whey+ Isolate"},
            {"MusclePharm", "Combat 100% Whey"},
            {"NutraOne", "Protein One"},
            {"PEScience", "Select Protein"},
            {"Primeval Labs", "WHEY"},
            {"Redcon1", "Isotope Whey Isolate"},
            {"Rivalus", "Rival Whey"},
            {"Transparent Labs", "100% Grass-Fed Whey"},
            {"Core Nutritionals", "Pro Whey"},
            {"Nutrabolics", "Hydropure Whey"},
            {"Diesel Nutrition", "New Zealand Whey"},
            {"Magnum Nutraceuticals", "Quattro Protein"},
            {"ANS Performance", "N-ISO Whey"},
            {"Panda Supplements", "Panda Whey"},
            {"Blackstone Labs", "Isolation"},
    };

    private static final String[][] MASS_LINES = {
            {"Optimum Nutrition", "Serious Mass"},
            {"MuscleTech", "Mass-Tech Extreme 2000"},
            {"Dymatize", "Super Mass Gainer"},
            {"BSN", "True-Mass 1200"},
            {"MyProtein", "Weight Gainer Blend"},
            {"Mutant", "Mass Triple Chocolate"},
            {"Ronnie Coleman", "King Mass XL"},
            {"GNC Pro Performance", "Bulk 1340"},
            {"Body Fortress", "Super Advanced Mass Gainer"},
            {"Evlution Nutrition", "Stacked Mass"},
            {"Rivalus", "Clean Gainer"},
            {"Primeval Labs", "Apesh*t Mass"},
            {"Redcon1", "MRE Whole Food Mass"},
            {"Panda Supplements", "Mass Gainer"},
            {"Nutrabolics", "Mammoth Mass"},
            {"ANS Performance", "N-Mass"},
            {"Magnum Nutraceuticals", "Quattro Mass"},
            {"Universal Nutrition", "Real Gains"},
            {"Animal", "M-Stak Mass"},
            {"GAT Sport", "JetMass"},
            {"Core Nutritionals", "Grow"},
            {"Rule 1", "R1 Gain"},
            {"MusclePharm", "Combat XL Mass Gainer"},
            {"Nutrex", "Mass Infusion"},
            {"Blackstone Labs", "Fast Grow"},
            {"Diesel Nutrition", "Mass Builder"},
            {"Ghost", "Size Gainer"},
            {"Isopure", "Mass Carb"},
            {"Cellucor", "Cor-Mass Gainer"},
            {"Legion Athletics", "Atlas Mass Gainer"},
    };

    private static final String[][] PRE_LINES = {
            {"Cellucor", "C4 Original"},
            {"Cellucor", "C4 Sport"},
            {"JYM Supplement Science", "Pre JYM"},
            {"Ghost", "Legend Pre-Workout"},
            {"Alani Nu", "Pre-Workout"},
            {"Bucked Up", "Woke AF"},
            {"Ryse", "Project Blackout Pre"},
            {"Transparent Labs", "Bulk Pre-Workout"},
            {"Legion Athletics", "Pulse"},
            {"Kaged Muscle", "Pre-Kaged"},
            {"Optimum Nutrition", "Gold Standard Pre"},
            {"MuscleTech", "Vapor X5"},
            {"BSN", "N.O.-Xplode"},
            {"Evlution Nutrition", "ENGN Pre-Workout"},
            {"Nutrex", "Outlift"},
            {"Redcon1", "Total War"},
            {"Primeval Labs", "Ape Sh*t Pre"},
            {"Panda Supplements", "Rampage Pre"},
            {"ANS Performance", "Dilate Pre"},
            {"Magnum Nutraceuticals", "Pre4"},
            {"Rivalus", "Compete Pre-Workout"},
            {"Rule 1", "R1 Pre Lift"},
            {"MusclePharm", "Assault Energy"},
            {"GAT Sport", "Nitraflex"},
            {"Core Nutritionals", "Fury Pre"},
            {"Dymatize", "Pre W.O."},
            {"MyProtein", "The Pre-Workout"},
            {"Universal Nutrition", "Animal Fury"},
            {"Ronnie Coleman", "Yeah Buddy"},
            {"Blackstone Labs", "Dust X"},
    };

    @Bean
    @Order(2)
    CommandLineRunner seedBulkCatalog(CategoryRepository categoryRepository,
                                      ProductRepository productRepository) {
        return args -> {
            long existing = productRepository.count();
            if (existing >= TARGET_PRODUCT_COUNT) {
                return;
            }

            int toAdd = TARGET_PRODUCT_COUNT - (int) existing;
            Category wheyCat = ensureCategory(categoryRepository, "Whey Protein", "whey-protein",
                    "High-quality whey protein powders for muscle recovery and growth");
            Category massCat = ensureCategory(categoryRepository, "Mass Gainer", "mass-gainer",
                    "Calorie-dense supplements for weight and muscle gain");
            Category preCat = ensureCategory(categoryRepository, "Pre-Workout", "pre-workout",
                    "Energy and performance boosting supplements");

            int base = toAdd / 3;
            int rem = toAdd % 3;
            int wheyN = base + (rem > 0 ? 1 : 0);
            int massN = base + (rem > 1 ? 1 : 0);
            int preN = base + (rem > 2 ? 1 : 0);

            int wheyStart = (int) productRepository.countByCategory_Id(wheyCat.getId()) + 1;
            int massStart = (int) productRepository.countByCategory_Id(massCat.getId()) + 1;
            int preStart = (int) productRepository.countByCategory_Id(preCat.getId()) + 1;

            for (int i = 0; i < wheyN; i++) {
                int seq = wheyStart + i;
                String[] line = WHEY_LINES[i % WHEY_LINES.length];
                Product p = buildProduct(
                        line[0] + " " + line[1] + " #" + seq,
                        "whey-" + String.format("%04d", seq),
                        "Whey protein blend phù hợp tập luyện và phục hồi — " + line[1] + ".",
                        line[0],
                        wheyCat
                );
                addWheyVariants(p, seq);
                productRepository.save(p);
            }
            for (int i = 0; i < massN; i++) {
                int seq = massStart + i;
                String[] line = MASS_LINES[i % MASS_LINES.length];
                Product p = buildProduct(
                        line[0] + " " + line[1] + " #" + seq,
                        "mass-" + String.format("%04d", seq),
                        "Mass gainer nhiều năng lượng cho người khó tăng cân — " + line[1] + ".",
                        line[0],
                        massCat
                );
                addMassVariants(p, seq);
                productRepository.save(p);
            }
            for (int i = 0; i < preN; i++) {
                int seq = preStart + i;
                String[] line = PRE_LINES[i % PRE_LINES.length];
                Product p = buildProduct(
                        line[0] + " " + line[1] + " #" + seq,
                        "pre-" + String.format("%04d", seq),
                        "Pre-workout tăng tỉnh táo và sức bền buổi tập — " + line[1] + ".",
                        line[0],
                        preCat
                );
                addPreVariants(p, seq);
                productRepository.save(p);
            }
        };
    }

    private static Category ensureCategory(CategoryRepository categoryRepository,
                                           String name, String slug, String description) {
        return categoryRepository.findBySlug(slug)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .slug(slug)
                        .description(description)
                        .build()));
    }

    private static Product buildProduct(String name, String slug, String description,
                                        String brand, Category category) {
        return Product.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .brand(brand)
                .category(category)
                .build();
    }

    private static void addWheyVariants(Product p, int seq) {
        List<Object[]> rows = List.of(
                new Object[]{"Chocolate", "2 lbs", new BigDecimal("729000"), "WH-" + seq + "-CHO-2"},
                new Object[]{"Vanilla", "2 lbs", new BigDecimal("729000"), "WH-" + seq + "-VAN-2"},
                new Object[]{"Cookies & Cream", "5 lbs", new BigDecimal("1490000"), "WH-" + seq + "-CCC-5"}
        );
        for (Object[] row : rows) {
            p.getVariants().add(ProductVariant.builder()
                    .flavor((String) row[0])
                    .weight((String) row[1])
                    .price((BigDecimal) row[2])
                    .sku((String) row[3])
                    .stock(20 + (seq % 40))
                    .product(p)
                    .build());
        }
    }

    private static void addMassVariants(Product p, int seq) {
        List<Object[]> rows = List.of(
                new Object[]{"Chocolate", "6 lbs", new BigDecimal("849000"), "MG-" + seq + "-CHO-6"},
                new Object[]{"Vanilla", "6 lbs", new BigDecimal("849000"), "MG-" + seq + "-VAN-6"},
                new Object[]{"Strawberry", "12 lbs", new BigDecimal("1590000"), "MG-" + seq + "-STR-12"}
        );
        for (Object[] row : rows) {
            p.getVariants().add(ProductVariant.builder()
                    .flavor((String) row[0])
                    .weight((String) row[1])
                    .price((BigDecimal) row[2])
                    .sku((String) row[3])
                    .stock(15 + (seq % 30))
                    .product(p)
                    .build());
        }
    }

    private static void addPreVariants(Product p, int seq) {
        List<Object[]> rows = List.of(
                new Object[]{"Fruit Punch", "30 servings", new BigDecimal("529000"), "PR-" + seq + "-FP-30"},
                new Object[]{"Blue Raspberry", "30 servings", new BigDecimal("529000"), "PR-" + seq + "-BR-30"},
                new Object[]{"Watermelon", "60 servings", new BigDecimal("949000"), "PR-" + seq + "-WM-60"}
        );
        for (Object[] row : rows) {
            p.getVariants().add(ProductVariant.builder()
                    .flavor((String) row[0])
                    .weight((String) row[1])
                    .price((BigDecimal) row[2])
                    .sku((String) row[3])
                    .stock(25 + (seq % 35))
                    .product(p)
                    .build());
        }
    }
}
