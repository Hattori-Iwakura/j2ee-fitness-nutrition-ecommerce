package com.example.j2ee_fitness_nutrition_ecommerce.service.ai;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Defines all function declarations for Gemini function calling.
 */
@Component
public class AgentToolDefinitions {

    public List<Map<String, Object>> getToolDefinitions() {
        return List.of(Map.of("function_declarations", List.of(
                searchProducts(),
                getProductDetail(),
                listCategories(),
                calculateTDEE(),
                recommendProducts(),
                addToCart(),
                getCart(),
                getOrderHistory(),
                getOrderStatus(),
                getWishlist()
        )));
    }

    private Map<String, Object> searchProducts() {
        return Map.of(
                "name", "searchProducts",
                "description", "Search for fitness supplement products by keyword and optional category. Returns product names, prices, brands.",
                "parameters", Map.of(
                        "type", "OBJECT",
                        "properties", Map.of(
                                "keyword", Map.of("type", "STRING", "description", "Search keyword (e.g. 'whey', 'mass gainer', 'vitamin')"),
                                "category", Map.of("type", "STRING", "description", "Category slug to filter (e.g. 'whey-protein', 'pre-workout'). Optional."),
                                "maxResults", Map.of("type", "INTEGER", "description", "Maximum number of results to return. Default 5.")
                        ),
                        "required", List.of("keyword")
                )
        );
    }

    private Map<String, Object> getProductDetail() {
        return Map.of(
                "name", "getProductDetail",
                "description", "Get detailed information about a specific product including all variants (flavors, weights, prices, stock), rating, and description.",
                "parameters", Map.of(
                        "type", "OBJECT",
                        "properties", Map.of(
                                "slug", Map.of("type", "STRING", "description", "Product slug (URL-friendly name)")
                        ),
                        "required", List.of("slug")
                )
        );
    }

    private Map<String, Object> listCategories() {
        return Map.of(
                "name", "listCategories",
                "description", "List all active product categories in the store.",
                "parameters", Map.of(
                        "type", "OBJECT",
                        "properties", Map.of()
                )
        );
    }

    private Map<String, Object> calculateTDEE() {
        return Map.of(
                "name", "calculateTDEE",
                "description", "Calculate Total Daily Energy Expenditure (TDEE) and recommended macros (protein, carbs, fat) based on user's body stats and fitness goal.",
                "parameters", Map.of(
                        "type", "OBJECT",
                        "properties", Map.of(
                                "gender", Map.of("type", "STRING", "description", "Gender: 'male' or 'female'"),
                                "age", Map.of("type", "INTEGER", "description", "Age in years"),
                                "weightKg", Map.of("type", "NUMBER", "description", "Body weight in kilograms"),
                                "heightCm", Map.of("type", "NUMBER", "description", "Height in centimeters"),
                                "activityLevel", Map.of("type", "STRING", "description", "Activity level: 'sedentary', 'light', 'moderate', 'active', 'very_active'"),
                                "goal", Map.of("type", "STRING", "description", "Fitness goal: 'lose' (weight loss), 'maintain', 'gain' (muscle gain)")
                        ),
                        "required", List.of("gender", "age", "weightKg", "heightCm", "activityLevel", "goal")
                )
        );
    }

    private Map<String, Object> recommendProducts() {
        return Map.of(
                "name", "recommendProducts",
                "description", "Recommend products based on a fitness goal (e.g. muscle gain, weight loss) and optional category preference.",
                "parameters", Map.of(
                        "type", "OBJECT",
                        "properties", Map.of(
                                "goal", Map.of("type", "STRING", "description", "Fitness goal: 'gain' (muscle/bulk), 'lose' (cut/diet), 'maintain', 'energy' (pre-workout)"),
                                "category", Map.of("type", "STRING", "description", "Optional category slug to narrow recommendations")
                        ),
                        "required", List.of("goal")
                )
        );
    }

    private Map<String, Object> addToCart() {
        return Map.of(
                "name", "addToCart",
                "description", "Add a product variant to the user's shopping cart. Requires the specific variant ID (get it from getProductDetail first).",
                "parameters", Map.of(
                        "type", "OBJECT",
                        "properties", Map.of(
                                "variantId", Map.of("type", "INTEGER", "description", "The product variant ID to add"),
                                "quantity", Map.of("type", "INTEGER", "description", "Quantity to add. Default 1.")
                        ),
                        "required", List.of("variantId")
                )
        );
    }

    private Map<String, Object> getCart() {
        return Map.of(
                "name", "getCart",
                "description", "Get the current contents of the user's shopping cart, including items, quantities, and total price.",
                "parameters", Map.of(
                        "type", "OBJECT",
                        "properties", Map.of()
                )
        );
    }

    private Map<String, Object> getOrderHistory() {
        return Map.of(
                "name", "getOrderHistory",
                "description", "Get the user's recent order history with order codes, statuses, totals, and dates.",
                "parameters", Map.of(
                        "type", "OBJECT",
                        "properties", Map.of()
                )
        );
    }

    private Map<String, Object> getOrderStatus() {
        return Map.of(
                "name", "getOrderStatus",
                "description", "Check the status and details of a specific order by its order code.",
                "parameters", Map.of(
                        "type", "OBJECT",
                        "properties", Map.of(
                                "orderCode", Map.of("type", "STRING", "description", "The order code (e.g. 'FIT-00001')")
                        ),
                        "required", List.of("orderCode")
                )
        );
    }

    private Map<String, Object> getWishlist() {
        return Map.of(
                "name", "getWishlist",
                "description", "Get the user's wishlist with saved products.",
                "parameters", Map.of(
                        "type", "OBJECT",
                        "properties", Map.of()
                )
        );
    }
}
