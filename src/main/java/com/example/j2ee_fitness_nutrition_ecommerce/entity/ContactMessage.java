package com.example.j2ee_fitness_nutrition_ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "contact_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String fullName;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(nullable = false, length = 4000)
    private String message;

    @Builder.Default
    private boolean read = false;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
