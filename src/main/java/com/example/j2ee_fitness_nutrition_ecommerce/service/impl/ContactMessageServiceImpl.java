package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.ContactMessage;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ContactMessageRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ContactMessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactMessageServiceImpl(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @Override
    @Transactional
    public ContactMessage save(ContactMessage message) {
        return contactMessageRepository.save(message);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactMessage> findAll(Pageable pageable) {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContactMessage> findById(Long id) {
        return contactMessageRepository.findById(id);
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        contactMessageRepository.findById(id).ifPresent(m -> {
            m.setRead(true);
            contactMessageRepository.save(m);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread() {
        return contactMessageRepository.countByReadFalse();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        contactMessageRepository.deleteById(id);
    }
}
