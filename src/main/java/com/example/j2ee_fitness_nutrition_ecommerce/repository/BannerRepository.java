package com.example.j2ee_fitness_nutrition_ecommerce.repository;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    List<Banner> findByActiveTrueOrderBySortOrderAscIdAsc();
}
