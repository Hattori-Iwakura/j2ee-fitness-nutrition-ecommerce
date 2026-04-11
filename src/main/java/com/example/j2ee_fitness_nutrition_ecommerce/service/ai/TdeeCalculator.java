package com.example.j2ee_fitness_nutrition_ecommerce.service.ai;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class TdeeCalculator {

    /**
     * Calculates TDEE and macros using the Mifflin-St Jeor equation.
     *
     * @param gender         "male" or "female"
     * @param age            age in years
     * @param weightKg       weight in kilograms
     * @param heightCm       height in centimeters
     * @param activityLevel  sedentary, light, moderate, active, very_active
     * @param goal           lose, maintain, gain
     */
    public Map<String, Object> calculate(String gender, int age, double weightKg,
                                          double heightCm, String activityLevel, String goal) {
        // Mifflin-St Jeor BMR
        double bmr;
        if ("female".equalsIgnoreCase(gender)) {
            bmr = 10 * weightKg + 6.25 * heightCm - 5 * age - 161;
        } else {
            bmr = 10 * weightKg + 6.25 * heightCm - 5 * age + 5;
        }

        double activityMultiplier = switch (activityLevel != null ? activityLevel.toLowerCase() : "moderate") {
            case "sedentary" -> 1.2;
            case "light" -> 1.375;
            case "moderate" -> 1.55;
            case "active" -> 1.725;
            case "very_active" -> 1.9;
            default -> 1.55;
        };

        double tdee = bmr * activityMultiplier;

        int goalAdjustment = switch (goal != null ? goal.toLowerCase() : "maintain") {
            case "lose" -> -500;
            case "gain" -> 500;
            default -> 0;
        };

        double goalCalories = tdee + goalAdjustment;

        // Macro calculation
        double proteinG = weightKg * 2.0; // 2g per kg for fitness
        double proteinCal = proteinG * 4;
        double fatCal = goalCalories * 0.25;
        double fatG = fatCal / 9;
        double carbCal = goalCalories - proteinCal - fatCal;
        double carbG = carbCal / 4;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bmr", Math.round(bmr));
        result.put("tdee", Math.round(tdee));
        result.put("goal_calories", Math.round(goalCalories));
        result.put("goal", goal);
        result.put("protein_grams", Math.round(proteinG));
        result.put("carbs_grams", Math.max(0, Math.round(carbG)));
        result.put("fat_grams", Math.round(fatG));
        result.put("protein_pct", Math.round(proteinCal / goalCalories * 100));
        result.put("carbs_pct", Math.max(0, Math.round(carbCal / goalCalories * 100)));
        result.put("fat_pct", 25);

        return result;
    }
}
