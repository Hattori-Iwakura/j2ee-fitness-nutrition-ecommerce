package com.example.j2ee_fitness_nutrition_ecommerce.service;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ContactMessageService {

    ContactMessage save(ContactMessage message);

    Page<ContactMessage> findAll(Pageable pageable);

    Optional<ContactMessage> findById(Long id);

    void markAsRead(Long id);

    void deleteById(Long id);

    long countUnread();
}
