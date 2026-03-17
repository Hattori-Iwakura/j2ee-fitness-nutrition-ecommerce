package com.example.j2ee_fitness_nutrition_ecommerce.repository;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Category;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.ProductVariant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ProductVariantRepositoryTest {

    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private TestEntityManager entityManager;

    @Test
    void findByProductIdAndActiveTrue_returnsOnlyActive() {
        Category cat = Category.builder().name("Protein").slug("protein").active(true).build();
        entityManager.persist(cat);

        Product product = Product.builder().name("Whey").slug("whey").active(true).category(cat).build();
        entityManager.persist(product);

        ProductVariant active = ProductVariant.builder().flavor("Chocolate").weight("2kg")
                .price(BigDecimal.valueOf(500000)).stock(10).active(true).product(product).build();
        ProductVariant inactive = ProductVariant.builder().flavor("Vanilla").weight("1kg")
                .price(BigDecimal.valueOf(300000)).stock(5).active(false).product(product).build();
        entityManager.persist(active);
        entityManager.persist(inactive);
        entityManager.flush();

        List<ProductVariant> result = variantRepository.findByProductIdAndActiveTrue(product.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFlavor()).isEqualTo("Chocolate");
    }
}
