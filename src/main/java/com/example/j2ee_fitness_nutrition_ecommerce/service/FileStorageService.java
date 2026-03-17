package com.example.j2ee_fitness_nutrition_ecommerce.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String store(MultipartFile file, String subdirectory);
    void delete(String filePath);
}
