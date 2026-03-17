package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Category;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryService categoryService;
    @InjectMocks private ProductServiceImpl productService;

    @Test
    void findAllActive_returnsActiveProducts() {
        Product product = Product.builder().id(1L).name("Whey").active(true).build();
        PageRequest pageable = PageRequest.of(0, 10);
        when(productRepository.findByActiveTrue(pageable))
                .thenReturn(new PageImpl<>(List.of(product)));

        Page<Product> result = productService.findAllActive(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Whey");
    }

    @Test
    void findActiveBySlug_returnsActiveProduct() {
        Product product = Product.builder().id(1L).slug("whey-protein").active(true).build();
        when(productRepository.findBySlugAndActiveTrue("whey-protein"))
                .thenReturn(Optional.of(product));

        Optional<Product> result = productService.findActiveBySlug("whey-protein");

        assertThat(result).isPresent();
    }

    @Test
    void findActiveBySlug_inactiveProduct_returnsEmpty() {
        when(productRepository.findBySlugAndActiveTrue("deleted-product"))
                .thenReturn(Optional.empty());

        Optional<Product> result = productService.findActiveBySlug("deleted-product");

        assertThat(result).isEmpty();
    }

    @Test
    void deleteById_softDelete_setsActiveToFalse() {
        Product product = Product.builder().id(1L).name("Whey").active(true).build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.deleteById(1L);

        assertThat(product.isActive()).isFalse();
        verify(productRepository).save(product);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteById_notFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteById(99L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void save_returnsProduct() {
        Product product = Product.builder().id(1L).name("New Product").build();
        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.save(product);

        assertThat(result.getName()).isEqualTo("New Product");
    }

    @Test
    void findAllBrands_returnsBrandList() {
        when(productRepository.findDistinctBrands())
                .thenReturn(List.of("Optimum Nutrition", "MuscleTech", "BSN"));

        List<String> brands = productService.findAllBrands();

        assertThat(brands).hasSize(3).contains("Optimum Nutrition", "MuscleTech");
    }

    @Test
    void findRelatedProducts_returnsRelated() {
        Product p1 = Product.builder().id(2L).name("Related 1").build();
        Product p2 = Product.builder().id(3L).name("Related 2").build();
        when(productRepository.findTop4ByCategoryIdAndActiveTrueAndIdNot(10L, 1L))
                .thenReturn(List.of(p1, p2));

        List<Product> result = productService.findRelatedProducts(1L, 10L);

        assertThat(result).hasSize(2);
    }
}
