package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.ChangePasswordRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.dto.ProfileUpdateRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.dto.RegisterRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.Role;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.UserRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.service.EmailService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    public User register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        User user = User.builder()
                .fullName(request.getFullName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.USER)
                .build();
        User savedUser = userRepository.save(user);
        emailService.sendWelcomeEmail(savedUser);
        return savedUser;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(email.trim());
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return userRepository.existsByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT));
    }

    @Override
    @Transactional
    public User updateProfile(String email, ProfileUpdateRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}

