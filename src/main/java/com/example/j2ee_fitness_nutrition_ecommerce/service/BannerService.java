package com.example.j2ee_fitness_nutrition_ecommerce.service;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Banner;

import java.util.List;
import java.util.Optional;

public interface BannerService {

    List<Banner> findActiveForHome();

    List<Banner> findAllForAdmin();

    Optional<Banner> findById(Long id);

    Banner save(Banner banner);

    void deleteById(Long id);
}
