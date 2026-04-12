package com.example.j2ee_fitness_nutrition_ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "banners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1024)
    private String imageUrl;

    /** Target URL when the banner is clicked (optional). */
    @Column(length = 1024)
    private String linkUrl;

    @Builder.Default
    private boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private int sortOrder = 0;
}
