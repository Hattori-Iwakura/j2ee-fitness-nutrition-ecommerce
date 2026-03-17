package com.example.j2ee_fitness_nutrition_ecommerce.repository;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        Category active = Category.builder().name("Protein").slug("protein").active(true).build();
        Category inactive = Category.builder().name("Old Cat").slug("old-cat").active(false).build();
        entityManager.persist(active);
        entityManager.persist(inactive);
        entityManager.flush();
    }

    @Test
    void findByActiveTrue_returnsOnlyActive() {
        List<Category> result = categoryRepository.findByActiveTrue();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Protein");
    }

    @Test
    void findBySlugAndActiveTrue_returnsCorrectCategory() {
        assertThat(categoryRepository.findBySlugAndActiveTrue("protein")).isPresent();
        assertThat(categoryRepository.findBySlugAndActiveTrue("old-cat")).isEmpty();
    }
}
