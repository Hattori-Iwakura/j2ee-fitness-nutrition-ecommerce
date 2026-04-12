package com.example.j2ee_fitness_nutrition_ecommerce.entity;

import com.example.j2ee_fitness_nutrition_ecommerce.enums.StockChangeType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "stock_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant variant;

    @Column(nullable = false)
    private int stockBefore;

    @Column(nullable = false)
    private int stockAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private StockChangeType changeType;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
