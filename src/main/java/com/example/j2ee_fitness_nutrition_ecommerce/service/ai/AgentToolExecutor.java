package com.example.j2ee_fitness_nutrition_ecommerce.service.ai;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.*;
import com.example.j2ee_fitness_nutrition_ecommerce.service.*;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AgentToolExecutor {

    private static final Logger log = LoggerFactory.getLogger(AgentToolExecutor.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;
    private final OrderService orderService;
    private final WishlistService wishlistService;
    private final ReviewService reviewService;
    private final TdeeCalculator tdeeCalculator;

    public AgentToolExecutor(ProductService productService, CategoryService categoryService,
                              CartService cartService, OrderService orderService,
                              WishlistService wishlistService, ReviewService reviewService,
                              TdeeCalculator tdeeCalculator) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.wishlistService = wishlistService;
        this.reviewService = reviewService;
        this.tdeeCalculator = tdeeCalculator;
    }

    public ToolResult execute(String functionName, Map<String, Object> args,
                               HttpSession session, String userEmail) {
        try {
            return switch (functionName) {
                case "searchProducts" -> searchProducts(args);
                case "getProductDetail" -> getProductDetail(args);
                case "listCategories" -> listCategories();
                case "calculateTDEE" -> calculateTDEE(args);
                case "recommendProducts" -> recommendProducts(args);
                case "addToCart" -> addToCart(args, session);
                case "getCart" -> getCart(session);
                case "getOrderHistory" -> getOrderHistory(userEmail);
                case "getOrderStatus" -> getOrderStatus(args, userEmail);
                case "getWishlist" -> getWishlist(userEmail);
                default -> ToolResult.error("Unknown tool: " + functionName);
            };
        } catch (Exception e) {
            log.error("Tool execution failed: {} with args {}", functionName, args, e);
            return ToolResult.error("Tool execution failed: " + e.getMessage());
        }
    }

    private ToolResult searchProducts(Map<String, Object> args) {
        String keyword = getString(args, "keyword", "");
        int maxResults = getInt(args, "maxResults", 5);

        Page<Product> page = productService.searchByName(keyword, PageRequest.of(0, maxResults));
        List<Map<String, Object>> products = page.getContent().stream()
                .filter(Product::isActive)
                .map(this::toProductSummary)
                .collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total_found", page.getTotalElements());
        data.put("products", products);

        return new ToolResult(data, "Searched for '" + keyword + "', found " + page.getTotalElements() + " products", true);
    }

    private ToolResult getProductDetail(Map<String, Object> args) {
        String slug = getString(args, "slug", "");
        Optional<Product> opt = productService.findActiveBySlug(slug);
        if (opt.isEmpty()) {
            return ToolResult.error("Product not found with slug: " + slug);
        }

        Product product = opt.get();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", product.getName());
        data.put("slug", product.getSlug());
        data.put("brand", product.getBrand());
        data.put("description", product.getDescription());
        data.put("category", product.getCategory() != null ? product.getCategory().getName() : null);
        data.put("imageUrl", product.getImageUrl());

        Double avgRating = reviewService.getAverageRating(product.getId());
        long reviewCount = reviewService.getReviewCount(product.getId());
        data.put("averageRating", avgRating != null ? Math.round(avgRating * 10) / 10.0 : null);
        data.put("reviewCount", reviewCount);

        List<Map<String, Object>> variants = product.getVariants().stream()
                .filter(ProductVariant::isActive)
                .map(v -> {
                    Map<String, Object> vm = new LinkedHashMap<>();
                    vm.put("variantId", v.getId());
                    vm.put("flavor", v.getFlavor());
                    vm.put("weight", v.getWeight());
                    vm.put("price", v.getPrice());
                    vm.put("inStock", v.getStock() > 0);
                    vm.put("stock", v.getStock());
                    return vm;
                })
                .collect(Collectors.toList());
        data.put("variants", variants);

        return new ToolResult(data, "Retrieved details for " + product.getName(), true);
    }

    private ToolResult listCategories() {
        List<Map<String, Object>> categories = categoryService.findAllActive().stream()
                .map(c -> {
                    Map<String, Object> cm = new LinkedHashMap<>();
                    cm.put("name", c.getName());
                    cm.put("slug", c.getSlug());
                    cm.put("description", c.getDescription());
                    return cm;
                })
                .collect(Collectors.toList());

        return new ToolResult(Map.of("categories", categories),
                "Listed " + categories.size() + " categories", true);
    }

    private ToolResult calculateTDEE(Map<String, Object> args) {
        String gender = getString(args, "gender", "male");
        int age = getInt(args, "age", 25);
        double weightKg = getDouble(args, "weightKg", 70);
        double heightCm = getDouble(args, "heightCm", 170);
        String activityLevel = getString(args, "activityLevel", "moderate");
        String goal = getString(args, "goal", "maintain");

        Map<String, Object> result = tdeeCalculator.calculate(gender, age, weightKg, heightCm, activityLevel, goal);
        return new ToolResult(result, "Calculated TDEE for " + gender + ", " + age + "y, " + weightKg + "kg", true);
    }

    private ToolResult recommendProducts(Map<String, Object> args) {
        String goal = getString(args, "goal", "gain");

        // Map goal to relevant search keywords
        List<String> keywords = switch (goal.toLowerCase()) {
            case "gain" -> List.of("whey", "mass gainer", "creatine");
            case "lose" -> List.of("whey isolate", "fat burner", "l-carnitine");
            case "energy" -> List.of("pre-workout", "caffeine", "bcaa");
            default -> List.of("whey", "vitamin", "multivitamin");
        };

        List<Map<String, Object>> allProducts = new ArrayList<>();
        for (String keyword : keywords) {
            Page<Product> page = productService.searchByName(keyword, PageRequest.of(0, 3));
            page.getContent().stream()
                    .filter(Product::isActive)
                    .map(this::toProductSummary)
                    .forEach(allProducts::add);
        }

        // Also add best sellers
        List<Product> bestSellers = productService.findBestSellers(3);
        bestSellers.stream()
                .map(this::toProductSummary)
                .forEach(allProducts::add);

        // Deduplicate by slug
        List<Map<String, Object>> unique = allProducts.stream()
                .collect(Collectors.toMap(
                        p -> (String) p.get("slug"),
                        p -> p,
                        (a, b) -> a,
                        LinkedHashMap::new
                ))
                .values().stream()
                .limit(6)
                .collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("goal", goal);
        data.put("recommendations", unique);

        return new ToolResult(data, "Recommended " + unique.size() + " products for goal: " + goal, true);
    }

    private ToolResult addToCart(Map<String, Object> args, HttpSession session) {
        long variantId = getLong(args, "variantId", 0);
        int quantity = getInt(args, "quantity", 1);

        if (variantId <= 0) {
            return ToolResult.error("Invalid variant ID");
        }
        if (quantity <= 0 || quantity > 10) {
            return ToolResult.error("Quantity must be between 1 and 10");
        }

        cartService.addToCart(session, variantId, quantity);

        // Get updated cart summary
        List<CartItem> cart = cartService.getCart(session);
        BigDecimal total = cartService.getCartTotal(session);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("success", true);
        data.put("message", "Added to cart successfully");
        data.put("cartItemCount", cart.size());
        data.put("cartTotal", total);

        return new ToolResult(data, "Added variant #" + variantId + " (qty: " + quantity + ") to cart", true);
    }

    private ToolResult getCart(HttpSession session) {
        List<CartItem> cart = cartService.getCart(session);
        BigDecimal total = cartService.getCartTotal(session);

        List<Map<String, Object>> items = cart.stream()
                .map(item -> {
                    Map<String, Object> im = new LinkedHashMap<>();
                    im.put("productName", item.getProductName());
                    im.put("flavor", item.getFlavor());
                    im.put("weight", item.getWeight());
                    im.put("price", item.getPrice());
                    im.put("quantity", item.getQuantity());
                    im.put("subtotal", item.getSubtotal());
                    return im;
                })
                .collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", items);
        data.put("itemCount", cart.size());
        data.put("total", total);

        return new ToolResult(data, "Cart has " + cart.size() + " items, total: " + total, true);
    }

    private ToolResult getOrderHistory(String userEmail) {
        List<Order> orders = orderService.findByUserEmail(userEmail);

        List<Map<String, Object>> orderList = orders.stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .limit(10)
                .map(o -> {
                    Map<String, Object> om = new LinkedHashMap<>();
                    om.put("orderCode", o.getOrderCode());
                    om.put("status", o.getStatus().name());
                    om.put("totalAmount", o.getTotalAmount());
                    om.put("createdAt", o.getCreatedAt() != null ? o.getCreatedAt().format(DATE_FMT) : null);
                    om.put("itemCount", o.getOrderDetails() != null ? o.getOrderDetails().size() : 0);
                    return om;
                })
                .collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orders", orderList);
        data.put("totalOrders", orders.size());

        return new ToolResult(data, "Found " + orders.size() + " orders", true);
    }

    private ToolResult getOrderStatus(Map<String, Object> args, String userEmail) {
        String orderCode = getString(args, "orderCode", "");
        Optional<Order> opt = orderService.findByOrderCode(orderCode);

        if (opt.isEmpty()) {
            return ToolResult.error("Order not found: " + orderCode);
        }

        Order order = opt.get();

        // Ownership check
        if (!order.getUser().getEmail().equals(userEmail)) {
            return ToolResult.error("Order not found: " + orderCode);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderCode", order.getOrderCode());
        data.put("status", order.getStatus().name());
        data.put("totalAmount", order.getTotalAmount());
        data.put("fullName", order.getFullName());
        data.put("phone", order.getPhone());
        data.put("address", order.getAddress());
        data.put("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_FMT) : null);

        if (order.getOrderDetails() != null) {
            List<Map<String, Object>> items = order.getOrderDetails().stream()
                    .map(d -> {
                        Map<String, Object> dm = new LinkedHashMap<>();
                        dm.put("productName", d.getVariant() != null && d.getVariant().getProduct() != null
                                ? d.getVariant().getProduct().getName() : "N/A");
                        dm.put("flavor", d.getVariant() != null ? d.getVariant().getFlavor() : null);
                        dm.put("weight", d.getVariant() != null ? d.getVariant().getWeight() : null);
                        dm.put("quantity", d.getQuantity());
                        dm.put("unitPrice", d.getUnitPrice());
                        dm.put("subtotal", d.getSubtotal());
                        return dm;
                    })
                    .collect(Collectors.toList());
            data.put("items", items);
        }

        return new ToolResult(data, "Order " + orderCode + " status: " + order.getStatus().name(), true);
    }

    private ToolResult getWishlist(String userEmail) {
        List<WishlistItem> wishlist = wishlistService.getWishlist(userEmail);

        List<Map<String, Object>> items = wishlist.stream()
                .map(w -> {
                    Product p = w.getProduct();
                    Map<String, Object> wm = new LinkedHashMap<>();
                    wm.put("productName", p.getName());
                    wm.put("slug", p.getSlug());
                    wm.put("brand", p.getBrand());
                    wm.put("category", p.getCategory() != null ? p.getCategory().getName() : null);
                    return wm;
                })
                .collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", items);
        data.put("count", items.size());

        return new ToolResult(data, "Wishlist has " + items.size() + " items", true);
    }

    // --- Helpers ---

    private Map<String, Object> toProductSummary(Product p) {
        Map<String, Object> pm = new LinkedHashMap<>();
        pm.put("name", p.getName());
        pm.put("slug", p.getSlug());
        pm.put("brand", p.getBrand());
        pm.put("category", p.getCategory() != null ? p.getCategory().getName() : null);
        pm.put("imageUrl", p.getImageUrl());

        List<ProductVariant> activeVariants = p.getVariants().stream()
                .filter(ProductVariant::isActive)
                .toList();

        if (!activeVariants.isEmpty()) {
            BigDecimal minPrice = activeVariants.stream()
                    .map(ProductVariant::getPrice)
                    .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal maxPrice = activeVariants.stream()
                    .map(ProductVariant::getPrice)
                    .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            pm.put("priceFrom", minPrice);
            pm.put("priceTo", maxPrice);
            pm.put("variantCount", activeVariants.size());
        }

        return pm;
    }

    private String getString(Map<String, Object> args, String key, String defaultVal) {
        Object val = args.get(key);
        return val != null ? val.toString() : defaultVal;
    }

    private int getInt(Map<String, Object> args, String key, int defaultVal) {
        Object val = args.get(key);
        if (val instanceof Number n) return n.intValue();
        if (val instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException e) { return defaultVal; }
        }
        return defaultVal;
    }

    private long getLong(Map<String, Object> args, String key, long defaultVal) {
        Object val = args.get(key);
        if (val instanceof Number n) return n.longValue();
        if (val instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException e) { return defaultVal; }
        }
        return defaultVal;
    }

    private double getDouble(Map<String, Object> args, String key, double defaultVal) {
        Object val = args.get(key);
        if (val instanceof Number n) return n.doubleValue();
        if (val instanceof String s) {
            try { return Double.parseDouble(s); } catch (NumberFormatException e) { return defaultVal; }
        }
        return defaultVal;
    }

    public record ToolResult(Map<String, Object> data, String actionSummary, boolean success) {
        public static ToolResult error(String message) {
            return new ToolResult(Map.of("error", message), message, false);
        }
    }
}
