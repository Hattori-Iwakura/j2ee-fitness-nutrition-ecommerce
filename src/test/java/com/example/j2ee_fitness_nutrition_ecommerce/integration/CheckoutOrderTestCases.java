package com.example.j2ee_fitness_nutrition_ecommerce.integration;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.CheckoutRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.ProductVariant;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.PaymentMethod;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.PaymentStatus;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductVariantRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.UserRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.service.CartItem;
import com.example.j2ee_fitness_nutrition_ecommerce.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shared assertions for MySQL-backed checkout flows (Testcontainers or host Docker MySQL).
 */
final class CheckoutOrderTestCases {

    private CheckoutOrderTestCases() {
    }

    static void assertCodOrderCreatesPaymentAndDeductsStock(
            OrderService orderService,
            UserRepository userRepository,
            ProductVariantRepository variantRepository) {

        User user = userRepository.findByEmailIgnoreCase("user@fitshop.com").orElseThrow();
        ProductVariant variant = variantRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("DataInitializer should have seeded variants"));

        int stockBefore = variant.getStock();
        int qty = Math.min(2, stockBefore);

        CheckoutRequest request = new CheckoutRequest();
        request.setFullName("Integration User");
        request.setPhone("0123456789");
        request.setAddress("123 Test Street");
        request.setPaymentMethod("COD");

        CartItem item = new CartItem(
                variant.getId(),
                variant.getProduct().getName(),
                variant.getFlavor(),
                variant.getWeight(),
                variant.getPrice(),
                qty,
                null
        );

        Order order = orderService.createOrder(user.getEmail(), request, List.of(item), null);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getOrderCode()).startsWith("FS-");
        assertThat(order.getTotalAmount()).isEqualByComparingTo(variant.getPrice().multiply(BigDecimal.valueOf(qty)));
        assertThat(order.getPayment()).isNotNull();
        assertThat(order.getPayment().getPaymentMethod()).isEqualTo(PaymentMethod.COD);
        assertThat(order.getPayment().getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);

        ProductVariant reloaded = variantRepository.findById(variant.getId()).orElseThrow();
        assertThat(reloaded.getStock()).isEqualTo(stockBefore - qty);
    }

    static void assertBankTransferOrderIsPending(
            OrderService orderService,
            UserRepository userRepository,
            ProductVariantRepository variantRepository) {

        User user = userRepository.findByEmailIgnoreCase("user@fitshop.com").orElseThrow();
        ProductVariant variant = variantRepository.findAll().stream()
                .skip(1)
                .findFirst()
                .orElseGet(() -> variantRepository.findAll().stream().findFirst().orElseThrow());

        CheckoutRequest request = new CheckoutRequest();
        request.setFullName("Bank User");
        request.setPhone("0987654321");
        request.setAddress("456 Bank Road");
        request.setPaymentMethod("BANK_TRANSFER");

        CartItem item = new CartItem(
                variant.getId(),
                variant.getProduct().getName(),
                variant.getFlavor(),
                variant.getWeight(),
                variant.getPrice(),
                1,
                null
        );

        Order order = orderService.createOrder(user.getEmail(), request, List.of(item), null);

        assertThat(order.getPayment().getPaymentMethod()).isEqualTo(PaymentMethod.BANK_TRANSFER);
        assertThat(order.getPayment().getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }
}
