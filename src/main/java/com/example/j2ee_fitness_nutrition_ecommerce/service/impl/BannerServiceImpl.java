package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Banner;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.BannerRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.service.BannerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    public BannerServiceImpl(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    @Override
    public List<Banner> findActiveForHome() {
        return bannerRepository.findByActiveTrueOrderBySortOrderAscIdAsc();
    }

    @Override
    public List<Banner> findAllForAdmin() {
        List<Banner> list = bannerRepository.findAll();
        list.sort(Comparator.comparingInt(Banner::getSortOrder).thenComparing(Banner::getId));
        return list;
    }

    @Override
    public Optional<Banner> findById(Long id) {
        return bannerRepository.findById(id);
    }

    @Override
    @Transactional
    public Banner save(Banner banner) {
        return bannerRepository.save(banner);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        bannerRepository.deleteById(id);
    }
}
