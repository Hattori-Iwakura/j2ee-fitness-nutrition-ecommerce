package com.example.j2ee_fitness_nutrition_ecommerce.config;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.OrderDetail;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Payment;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.ProductVariant;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Review;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.OrderStatus;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.PaymentMethod;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.PaymentStatus;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.OrderRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductVariantRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ReviewRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Inserts 30 demo orders for {@code user@fitshop.com} when {@code DEMO-SEED-001} is missing;
 * the first 10 are {@code DELIVERED} with distinct products; the other 20 rotate other statuses.
 * <p>On every startup (when enabled): ensures the first 10 {@code DEMO-SEED-*} orders are
 * {@code DELIVERED}, then backfills up to 10 reviews for {@code user@fitshop.com} so admin
 * Reviews is populated even if orders were seeded before review seed existed.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.demo-orders-seed", name = "enabled", havingValue = "true")
public class DemoOrdersSeeder {

    private static final String SEED_PREFIX = "DEMO-SEED-";
    private static final int DEMO_ORDER_COUNT = 30;
    private static final int DELIVERED_ORDER_COUNT = 10;
    private static final int DEMO_REVIEW_COUNT = 10;

    private static final OrderStatus[] NON_DELIVERED_ROTATION = {
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.SHIPPING,
            OrderStatus.CANCELLED
    };

    private static final String[] DEMO_REVIEW_COMMENTS = {
            "Giao nhanh, đóng gói cẩn thận. Dùng được 2 tuần thấy ổn.",
            "Hương vị dễ uống, tan nhanh. Sẽ mua lại.",
            "Giá hợp lý so với chất lượng. Tập xong phục hồi tốt.",
            "Sp đúng mô tả, date còn xa. Recommend.",
            "Mình dùng pre này tỉnh táo hơn hẳn, không quá gắt.",
            "Mass gainer tăng cân đều, không bị đầy bụng.",
            "Whey chocolate thơm, pha sữa rất hợp.",
            "Giao đúng hẹn, shipper nhiệt tình.",
            "Chất lượng ổn cho người mới tập, mình sẽ thử size lớn hơn sau.",
            "Sản phẩm chính hãng, check mã ok. Support trả lời nhanh."
    };

    private static final int[] DEMO_REVIEW_RATINGS = {5, 5, 4, 5, 4, 5, 5, 4, 5, 4};

    private static final String[] VI_NAMES = {
            "Nguyễn Văn An", "Trần Thị Bình", "Lê Hoàng Nam", "Phạm Thu Hà", "Hoàng Minh Tuấn",
            "Võ Thị Mai", "Đặng Quốc Huy", "Bùi Thị Lan", "Đỗ Văn Hùng", "Ngô Thị Yến",
            "Dương Minh Khang", "Lý Thị Hương", "Trương Văn Phúc", "Phan Thị Ngọc", "Vũ Đức Thịnh",
            "Tăng Thị Hồng", "Mai Văn Long", "Lưu Thị Nga", "Châu Minh Đức", "Hồ Thị Kim Oanh",
            "Tôn Văn Sơn", "Quách Thị Diễm", "La Hoàng Phát", "Cao Thị Xuân", "Kiều Văn Tài",
            "Lâm Thị Phượng", "Từ Minh Quân", "Hà Thị Thuỳ", "Giang Văn Hiếu", "Thạch Thị Vy"
    };

    private static final String[] VI_ADDRESSES = {
            "12 Nguyễn Huệ, Q.1, TP.HCM",
            "45 Lê Lợi, Q.1, TP.HCM",
            "88 Trần Hưng Đạo, Q.5, TP.HCM",
            "30 Phan Đình Phùng, Ba Đình, Hà Nội",
            "9 Kim Mã, Ba Đình, Hà Nội",
            "120 Nguyễn Văn Cừ, Long Biên, Hà Nội",
            "56 Lê Duẩn, Hải Châu, Đà Nẵng",
            "18 Bạch Đằng, Hải Châu, Đà Nẵng",
            "7 Nguyễn Tất Thành, Nha Trang, Khánh Hòa",
            "22 Hùng Vương, TP. Huế"
    };

    @Bean
    @org.springframework.core.annotation.Order(3)
    CommandLineRunner seedDemoOrders(PlatformTransactionManager transactionManager,
                                     OrderRepository orderRepository,
                                     UserRepository userRepository,
                                     ProductVariantRepository variantRepository,
                                     ReviewRepository reviewRepository) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        return args -> tx.executeWithoutResult(status -> {
            User buyer = userRepository.findByEmail("user@fitshop.com").orElse(null);
            if (buyer == null) {
                return;
            }

            if (orderRepository.findByOrderCode(SEED_PREFIX + "001").isEmpty()) {
                seedThirtyOrdersWithReviews(orderRepository, variantRepository, reviewRepository, buyer);
            }

            normalizeFirstTenDemoOrdersToDelivered(orderRepository);
            backfillDemoReviewsFromDemoOrders(orderRepository, reviewRepository, variantRepository, buyer);
        });
    }

    private void seedThirtyOrdersWithReviews(OrderRepository orderRepository,
                                             ProductVariantRepository variantRepository,
                                             ReviewRepository reviewRepository,
                                             User buyer) {
        List<ProductVariant> variants = variantRepository.findAll(PageRequest.of(0, 200)).getContent();
        if (variants.isEmpty()) {
            return;
        }

        List<ProductVariant> oneVariantPerProduct = pickDistinctProductVariants(variants, DELIVERED_ORDER_COUNT);
        if (oneVariantPerProduct.size() < DELIVERED_ORDER_COUNT) {
            return;
        }

        PaymentMethod[] payMethods = PaymentMethod.values();
        List<Product> productsToReview = new ArrayList<>();

        for (int i = 1; i <= DEMO_ORDER_COUNT; i++) {
            final ProductVariant v1;
            final ProductVariant v2;
            final OrderStatus orderStatus;

            if (i <= DELIVERED_ORDER_COUNT) {
                v1 = oneVariantPerProduct.get(i - 1);
                v2 = pickSecondaryVariant(variants, v1, i);
                orderStatus = OrderStatus.DELIVERED;
                productsToReview.add(v1.getProduct());
            } else {
                v1 = variants.get((i * 3) % variants.size());
                v2 = variants.get((i * 7 + 1) % variants.size());
                orderStatus = NON_DELIVERED_ROTATION[(i - DELIVERED_ORDER_COUNT - 1) % NON_DELIVERED_ROTATION.length];
            }

            int qty1 = 1 + (i % 3);
            int qty2 = (i % 4 == 0) ? 0 : (1 + (i % 2));
            if (v2.getId().equals(v1.getId())) {
                qty2 = 0;
            }

            BigDecimal sub1 = v1.getPrice().multiply(BigDecimal.valueOf(qty1));
            BigDecimal sub2 = qty2 > 0 ? v2.getPrice().multiply(BigDecimal.valueOf(qty2)) : BigDecimal.ZERO;
            BigDecimal total = sub1.add(sub2);

            Order order = Order.builder()
                    .orderCode(SEED_PREFIX + String.format("%03d", i))
                    .status(orderStatus)
                    .totalAmount(total)
                    .discountAmount(BigDecimal.ZERO)
                    .fullName(VI_NAMES[(i - 1) % VI_NAMES.length])
                    .phone("09" + String.format("%08d", (12345670 + i * 137) % 100000000))
                    .address(VI_ADDRESSES[(i - 1) % VI_ADDRESSES.length])
                    .note(i % 5 == 0 ? "Giao buổi chiều, gọi trước 30 phút." : null)
                    .createdAt(LocalDateTime.now().minusDays(DEMO_ORDER_COUNT - i).minusHours(i % 12))
                    .user(buyer)
                    .orderDetails(new ArrayList<>())
                    .build();

            order.getOrderDetails().add(OrderDetail.builder()
                    .order(order)
                    .variant(v1)
                    .quantity(qty1)
                    .unitPrice(v1.getPrice())
                    .subtotal(sub1)
                    .build());
            if (qty2 > 0) {
                order.getOrderDetails().add(OrderDetail.builder()
                        .order(order)
                        .variant(v2)
                        .quantity(qty2)
                        .unitPrice(v2.getPrice())
                        .subtotal(sub2)
                        .build());
            }

            PaymentMethod method = payMethods[(i - 1) % payMethods.length];
            Payment payment = buildPayment(order, method, i);
            order.setPayment(payment);
            payment.setOrder(order);

            orderRepository.save(order);
        }

        saveDemoReviewsForProducts(reviewRepository, buyer, productsToReview);
    }

    private static void normalizeFirstTenDemoOrdersToDelivered(OrderRepository orderRepository) {
        List<Order> demo = orderRepository.findByOrderCodeStartingWithOrderByOrderCodeAsc(SEED_PREFIX);
        for (int i = 0; i < Math.min(DELIVERED_ORDER_COUNT, demo.size()); i++) {
            Order o = demo.get(i);
            if (o.getStatus() != OrderStatus.DELIVERED) {
                o.setStatus(OrderStatus.DELIVERED);
                orderRepository.save(o);
            }
        }
    }

    private void backfillDemoReviewsFromDemoOrders(OrderRepository orderRepository,
                                                   ReviewRepository reviewRepository,
                                                   ProductVariantRepository variantRepository,
                                                   User buyer) {
        List<Order> demo = orderRepository.findByOrderCodeStartingWithOrderByOrderCodeAsc(SEED_PREFIX);
        if (demo.isEmpty()) {
            return;
        }

        List<Product> products = new ArrayList<>();
        Set<Long> seenProductIds = new LinkedHashSet<>();
        for (int i = 0; i < Math.min(DELIVERED_ORDER_COUNT, demo.size()); i++) {
            Order o = demo.get(i);
            if (o.getOrderDetails() == null || o.getOrderDetails().isEmpty()) {
                continue;
            }
            Product p = o.getOrderDetails().get(0).getVariant().getProduct();
            if (seenProductIds.add(p.getId())) {
                products.add(p);
            }
        }

        if (products.size() < DEMO_REVIEW_COUNT) {
            List<ProductVariant> variants = variantRepository.findAll(PageRequest.of(0, 200)).getContent();
            for (ProductVariant v : pickDistinctProductVariants(variants, 200)) {
                if (products.size() >= DEMO_REVIEW_COUNT) {
                    break;
                }
                Product p = v.getProduct();
                if (seenProductIds.add(p.getId())) {
                    products.add(p);
                }
            }
        }

        saveDemoReviewsForProducts(reviewRepository, buyer, products);
    }

    private static void saveDemoReviewsForProducts(ReviewRepository reviewRepository,
                                                   User buyer,
                                                   List<Product> products) {
        LocalDateTime reviewBase = LocalDateTime.now().minusDays(5);
        for (int r = 0; r < DEMO_REVIEW_COUNT && r < products.size(); r++) {
            Product product = products.get(r);
            if (reviewRepository.existsByUserIdAndProductIdAndDeletedFalse(buyer.getId(), product.getId())) {
                continue;
            }
            reviewRepository.save(Review.builder()
                    .user(buyer)
                    .product(product)
                    .rating(DEMO_REVIEW_RATINGS[r])
                    .comment(DEMO_REVIEW_COMMENTS[r])
                    .createdAt(reviewBase.plusHours(r))
                    .deleted(false)
                    .build());
        }
    }

    /**
     * First variant encountered per product id, preserving catalog order (up to {@code limit} products).
     */
    private static List<ProductVariant> pickDistinctProductVariants(List<ProductVariant> variants, int limit) {
        Set<Long> seenProductIds = new LinkedHashSet<>();
        List<ProductVariant> out = new ArrayList<>();
        for (ProductVariant v : variants) {
            Long pid = v.getProduct().getId();
            if (seenProductIds.add(pid)) {
                out.add(v);
                if (out.size() >= limit) {
                    break;
                }
            }
        }
        return out;
    }

    private static ProductVariant pickSecondaryVariant(List<ProductVariant> all, ProductVariant primary, int orderIndex) {
        for (int k = 0; k < all.size(); k++) {
            ProductVariant candidate = all.get((orderIndex * 7 + 3 + k) % all.size());
            if (!candidate.getId().equals(primary.getId())
                    && !candidate.getProduct().getId().equals(primary.getProduct().getId())) {
                return candidate;
            }
        }
        return primary;
    }

    private static Payment buildPayment(com.example.j2ee_fitness_nutrition_ecommerce.entity.Order order,
                                        PaymentMethod method, int index) {
        Payment.PaymentBuilder b = Payment.builder()
                .order(order)
                .paymentMethod(method)
                .transactionCode("PAY-DEMO-" + String.format("%05d", index));

        return switch (method) {
            case COD -> b.paymentStatus(PaymentStatus.COMPLETED)
                    .paidAt(order.getCreatedAt().plusHours(1))
                    .build();
            case BANK_TRANSFER -> {
                boolean completed = index % 3 != 0;
                if (completed) {
                    yield b.paymentStatus(PaymentStatus.COMPLETED)
                            .paidAt(order.getCreatedAt().plusDays(1))
                            .build();
                }
                yield b.paymentStatus(PaymentStatus.PENDING).build();
            }
            case E_WALLET -> b.paymentStatus(PaymentStatus.COMPLETED)
                    .paidAt(order.getCreatedAt().plusMinutes(30))
                    .build();
        };
    }
}
