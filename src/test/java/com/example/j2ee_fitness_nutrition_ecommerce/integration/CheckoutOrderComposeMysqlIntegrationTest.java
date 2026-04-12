package com.example.j2ee_fitness_nutrition_ecommerce.integration;

import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductVariantRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.UserRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hits MySQL on the host (e.g. {@code docker compose up mysql} on port 3306).
 * <p>
 * Run only when you intend to validate against that DB:
 * <pre>
 *   set INTEGRATION_TEST_USE_HOST_MYSQL=true
 *   mvn test -Dtest=CheckoutOrderComposeMysqlIntegrationTest
 * </pre>
 * Optional overrides: {@code INTEGRATION_TEST_JDBC_URL}, {@code INTEGRATION_TEST_JDBC_USER},
 * {@code INTEGRATION_TEST_JDBC_PASSWORD}.
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@EnabledIfEnvironmentVariable(named = "INTEGRATION_TEST_USE_HOST_MYSQL", matches = "true")
@Transactional
class CheckoutOrderComposeMysqlIntegrationTest {

    @DynamicPropertySource
    static void hostMysql(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getenv().getOrDefault(
                "INTEGRATION_TEST_JDBC_URL",
                "jdbc:mysql://127.0.0.1:3306/fitness_nutrition_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"));
        registry.add("spring.datasource.username", () -> System.getenv().getOrDefault(
                "INTEGRATION_TEST_JDBC_USER", "fitness_user"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault(
                "INTEGRATION_TEST_JDBC_PASSWORD", "fitness_pass"));
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
