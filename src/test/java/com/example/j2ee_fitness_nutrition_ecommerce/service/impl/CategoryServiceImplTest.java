package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Category;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private CategoryServiceImpl categoryService;

    @Test
    void findAllActive_returnsActiveCategories() {
        Category cat = Category.builder().id(1L).name("Whey Protein").active(true).build();
        when(categoryRepository.findByActiveTrue()).thenReturn(List.of(cat));

        List<Category> result = categoryService.findAllActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Whey Protein");
    }

    @Test
    void findActiveBySlug_returnsActiveCategory() {
        Category cat = Category.builder().id(1L).slug("whey-protein").active(true).build();
        when(categoryRepository.findBySlugAndActiveTrue("whey-protein"))
                .thenReturn(Optional.of(cat));

        Optional<Category> result = categoryService.findActiveBySlug("whey-protein");

        assertThat(result).isPresent();
    }

    @Test
    void deleteById_softDelete_setsActiveToFalse() {
        Category cat = Category.builder().id(1L).name("Old Category").active(true).build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        categoryService.deleteById(1L);

        assertThat(cat.isActive()).isFalse();
        verify(categoryRepository).save(cat);
        verify(categoryRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteById_notFound_throwsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteById(99L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void save_returnsCategory() {
        Category cat = Category.builder().name("New Cat").slug("new-cat").build();
        when(categoryRepository.save(cat)).thenReturn(cat);

        Category result = categoryService.save(cat);

        assertThat(result.getName()).isEqualTo("New Cat");
    }
}
