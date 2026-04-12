package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.CheckoutRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.*;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.OrderStatus;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.OrderRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductVariantRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.UserRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.PaymentMethod;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.StockChangeType;
import com.example.j2ee_fitness_nutrition_ecommerce.service.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository variantRepository;
    private final CouponService couponService;
    private final PaymentService paymentService;
    private final EmailService emailService;
    private final StockLogService stockLogService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            UserRepository userRepository,
                            ProductVariantRepository variantRepository,
                            CouponService couponService,
                            PaymentService paymentService,
                            EmailService emailService,
                            StockLogService stockLogService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.variantRepository = variantRepository;
        this.couponService = couponService;
        this.paymentService = paymentService;
        this.emailService = emailService;
        this.stockLogService = stockLogService;
    }

    @Override
    @Transactional
    public Order createOrder(String userEmail, CheckoutRequest request, List<CartItem> cartItems, String couponCode) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new IllegalStateException(
                        "Tài khoản không tồn tại trong hệ thống. Vui lòng đăng nhập lại."));

        Order order = Order.builder()
                .orderCode("FS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .note(request.getNote())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            ProductVariant variant = variantRepository.findById(cartItem.getVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Variant not found"));

            if (variant.getStock() < cartItem.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for " + cartItem.getProductName()
                        + " (" + cartItem.getFlavor() + " - " + cartItem.getWeight() + ")");
            }

            int stockBefore = variant.getStock();
            variant.setStock(stockBefore - cartItem.getQuantity());
            variantRepository.save(variant);
            stockLogService.log(variant, stockBefore, variant.getStock(), StockChangeType.SOLD);

            BigDecimal subtotal = variant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .variant(variant)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(variant.getPrice())
                    .subtotal(subtotal)
                    .build();

            order.getOrderDetails().add(detail);
            total = total.add(subtotal);
        }

        // Apply coupon discount if provided
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (couponCode != null && !couponCode.isBlank()) {
            Coupon coupon = couponService.validate(couponCode, total);
            discountAmount = couponService.calculateDiscount(coupon, total);
            order.setCoupon(coupon);
            order.setDiscountAmount(discountAmount);
            couponService.incrementUsage(coupon);
        }

        order.setTotalAmount(total.subtract(discountAmount));
        Order savedOrder = orderRepository.save(order);

        // Create payment
        PaymentMethod paymentMethod = parsePaymentMethod(request.getPaymentMethod());
        paymentService.createPayment(savedOrder, paymentMethod);

        // Send order confirmation email
        emailService.sendOrderConfirmation(savedOrder);

        return savedOrder;
    }

    @Override
    public List<Order> findByUserEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public Optional<Order> findByOrderCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Optional<Order> findByOrderCodeForView(String orderCode) {
        Optional<Order> opt = orderRepository.findByOrderCode(orderCode);
        if (opt.isEmpty()) {
            return opt;
        }
        Order o = opt.get();
        // Touch lazy associations while session is open (avoids LazyInitializationException on success/payment views).
        o.getUser().getEmail();
        if (o.getPayment() != null) {
            o.getPayment().getPaymentMethod();
        }
        if (o.getOrderDetails() != null) {
            for (OrderDetail od : o.getOrderDetails()) {
                if (od.getVariant() != null) {
                    var v = od.getVariant();
                    v.getFlavor();
                    if (v.getProduct() != null) {
                        var p = v.getProduct();
                        p.getName();
                        if (p.getCategory() != null) {
                            p.getCategory().getId();
                        }
                        if (p.getVariants() != null) {
                            p.getVariants().size();
                        }
                    }
                }
            }
        }
        return opt;
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    public Page<Order> findByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    @Override
    public long countByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Override
    @Transactional
    public Order updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    private static PaymentMethod parsePaymentMethod(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("Vui lòng chọn phương thức thanh toán.");
        }
        try {
            return PaymentMethod.valueOf(raw.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Phương thức thanh toán không hợp lệ.");
        }
    }
}
