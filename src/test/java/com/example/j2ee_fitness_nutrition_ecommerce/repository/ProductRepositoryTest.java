package com.example.j2ee_fitness_nutrition_ecommerce.repository;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Category;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired private ProductRepository productRepository;
    @Autowired private TestEntityManager entityManager;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder().name("Protein").slug("protein").active(true).build();
        entityManager.persist(category);

        Product active = Product.builder().name("Whey Gold").slug("whey-gold")
                .active(true).category(category).build();
        Product inactive = Product.builder().name("Old Product").slug("old-product")
                .active(false).category(category).build();
        entityManager.persist(active);
        entityManager.persist(inactive);
        entityManager.flush();
    }

    @Test
    void findByActiveTrue_returnsOnlyActiveProducts() {
        Page<Product> result = productRepository.findByActiveTrue(PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Whey Gold");
    }

    @Test
    void findByCategoryIdAndActiveTrue_filtersCorrectly() {
        Page<Product> result = productRepository.findByCategoryIdAndActiveTrue(
                category.getId(), PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findByNameContainingIgnoreCaseAndActiveTrue_searchWorks() {
        Page<Product> result = productRepository.findByNameContainingIgnoreCaseAndActiveTrue(
                "whey", PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Whey Gold");
    }

    @Test
    void findBySlugAndActiveTrue_returnsCorrectProduct() {
        Optional<Product> result = productRepository.findBySlugAndActiveTrue("whey-gold");
        assertThat(result).isPresent();

        Optional<Product> inactive = productRepository.findBySlugAndActiveTrue("old-product");
        assertThat(inactive).isEmpty();
    }
}
