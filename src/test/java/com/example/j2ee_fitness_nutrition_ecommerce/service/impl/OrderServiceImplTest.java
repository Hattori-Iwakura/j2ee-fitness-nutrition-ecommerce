package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.CheckoutRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.*;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.OrderStatus;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.PaymentMethod;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.OrderRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductVariantRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.UserRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private CouponService couponService;
    @Mock private PaymentService paymentService;
    @Mock private EmailService emailService;
    @Mock private StockLogService stockLogService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private ProductVariant variant;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@test.com").fullName("Test User").build();
        product = Product.builder().id(1L).name("Whey Protein").slug("whey-protein").build();
        variant = ProductVariant.builder()
                .id(1L).product(product).flavor("Chocolate").weight("2kg")
                .price(new BigDecimal("500000")).stock(10).active(true)
                .build();
    }

    @Test
    void createOrder_success() {
        CheckoutRequest request = new CheckoutRequest();
        request.setFullName("Test User");
        request.setPhone("0123456789");
        request.setAddress("123 Street");
        request.setPaymentMethod("COD");

        CartItem cartItem = new CartItem(1L, "Whey Protein", "Chocolate", "2kg",
                new BigDecimal("500000"), 2, null);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        Order result = orderService.createOrder("user@test.com", request, List.of(cartItem), null);

        assertThat(result).isNotNull();
        assertThat(result.getOrderCode()).startsWith("FS-");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("1000000");
        assertThat(result.getOrderDetails()).hasSize(1);
        assertThat(variant.getStock()).isEqualTo(8); // 10 - 2
        verify(paymentService).createPayment(any(Order.class), eq(PaymentMethod.COD));
        verify(emailService).sendOrderConfirmation(any(Order.class));
    }

    @Test
    void createOrder_withCoupon() {
        CheckoutRequest request = new CheckoutRequest();
        request.setFullName("Test User");
        request.setPhone("0123456789");
        request.setAddress("123 Street");
        request.setPaymentMethod("COD");

        CartItem cartItem = new CartItem(1L, "Whey Protein", "Chocolate", "2kg",
                new BigDecimal("500000"), 2, null);

        Coupon coupon = Coupon.builder().id(1L).code("SAVE10").active(true).build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));
        when(couponService.validate(eq("SAVE10"), any(BigDecimal.class))).thenReturn(coupon);
        when(couponService.calculateDiscount(eq(coupon), any(BigDecimal.class)))
                .thenReturn(new BigDecimal("100000"));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        Order result = orderService.createOrder("user@test.com", request, List.of(cartItem), "SAVE10");

        assertThat(result.getTotalAmount()).isEqualByComparingTo("900000");
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("100000");
        assertThat(result.getCoupon()).isEqualTo(coupon);
        verify(couponService).incrementUsage(coupon);
    }

    @Test
    void createOrder_insufficientStock_throwsException() {
        CheckoutRequest request = new CheckoutRequest();
        request.setFullName("Test User");
        request.setPhone("0123456789");
        request.setAddress("123 Street");
        request.setPaymentMethod("COD");

        variant.setStock(1);
        CartItem cartItem = new CartItem(1L, "Whey Protein", "Chocolate", "2kg",
                new BigDecimal("500000"), 5, null);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));

        assertThatThrownBy(() -> orderService.createOrder("user@test.com", request, List.of(cartItem), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void updateStatus_success() {
        Order order = Order.builder().id(1L).status(OrderStatus.PENDING).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.updateStatus(1L, OrderStatus.CONFIRMED);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void updateStatus_orderNotFound_throwsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(99L, OrderStatus.CONFIRMED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findByUserEmail_success() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        List<Order> result = orderService.findByUserEmail("user@test.com");

        assertThat(result).isEmpty();
        verify(orderRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void findByOrderCode_success() {
        Order order = Order.builder().id(1L).orderCode("FS-ABCD1234").build();
        when(orderRepository.findByOrderCode("FS-ABCD1234")).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.findByOrderCode("FS-ABCD1234");

        assertThat(result).isPresent();
        assertThat(result.get().getOrderCode()).isEqualTo("FS-ABCD1234");
    }
}
