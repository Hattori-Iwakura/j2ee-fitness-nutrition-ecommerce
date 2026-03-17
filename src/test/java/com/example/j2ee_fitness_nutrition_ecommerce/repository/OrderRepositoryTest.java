package com.example.j2ee_fitness_nutrition_ecommerce.repository;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.OrderStatus;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired private OrderRepository orderRepository;
    @Autowired private TestEntityManager entityManager;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().fullName("Test User").email("user@test.com")
                .password("encoded").role(Role.USER).build();
        entityManager.persist(user);

        Order order1 = Order.builder().orderCode("FS-001").status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(500000)).fullName("Test")
                .phone("012").address("Addr").user(user).build();
        Order order2 = Order.builder().orderCode("FS-002").status(OrderStatus.DELIVERED)
                .totalAmount(BigDecimal.valueOf(300000)).fullName("Test")
                .phone("012").address("Addr").user(user).build();
        entityManager.persist(order1);
        entityManager.persist(order2);
        entityManager.flush();
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_returnsOrdered() {
        List<Order> result = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        assertThat(result).hasSize(2);
    }

    @Test
    void findByOrderCode_returnsCorrectOrder() {
        Optional<Order> result = orderRepository.findByOrderCode("FS-001");
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void findByStatus_filtersCorrectly() {
        Page<Order> result = orderRepository.findByStatus(OrderStatus.PENDING, PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getOrderCode()).isEqualTo("FS-001");
    }
}
