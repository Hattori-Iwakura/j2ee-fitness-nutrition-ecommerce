package com.example.j2ee_fitness_nutrition_ecommerce.integration;

import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductVariantRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.UserRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * MySQL 8 via Testcontainers (Docker). Starts its own container — does not use {@code docker compose} DB.
 * Requires Docker running; skipped automatically if Docker is unavailable.
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class CheckoutOrderMySqlIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("fitness_nutrition_db")
            .withUsername("fitness_user")
            .withPassword("fitness_pass");

    @DynamicPropertySource
    static void registerMysql(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductVariantRepository variantRepository;

    @Test
    void createOrder_cod_persistsOrderPaymentAndDeductsStock() {
        CheckoutOrderTestCases.assertCodOrderCreatesPaymentAndDeductsStock(
                orderService, userRepository, variantRepository);
    }

    @Test
    void createOrder_bankTransfer_paymentPendingUntilConfirmed() {
        CheckoutOrderTestCases.assertBankTransferOrderIsPending(
                orderService, userRepository, variantRepository);
    }
}
