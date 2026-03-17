package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.RegisterRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.Role;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.UserRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @InjectMocks private UserServiceImpl userService;

    @Test
    void register_encodesPasswordAndSetsRoleUser() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("John Doe");
        request.setEmail("john@test.com");
        request.setPassword("password123");
        request.setPhone("0123456789");

        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.register(request);

        assertThat(result.getPassword()).isEqualTo("encoded_password");
        assertThat(result.getRole()).isEqualTo(Role.USER);
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_setsAllFieldsFromRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Jane Doe");
        request.setEmail("jane@test.com");
        request.setPassword("secret");
        request.setPhone("0987654321");

        when(passwordEncoder.encode(anyString())).thenReturn("enc");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.register(request);

        assertThat(result.getFullName()).isEqualTo("Jane Doe");
        assertThat(result.getEmail()).isEqualTo("jane@test.com");
        assertThat(result.getPhone()).isEqualTo("0987654321");
        verify(emailService).sendWelcomeEmail(any(User.class));
    }

    @Test
    void findByEmail_delegatesToRepository() {
        User user = User.builder().id(1L).email("test@test.com").build();
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail("test@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void existsByEmail_delegatesToRepository() {
        when(userRepository.existsByEmail("exists@test.com")).thenReturn(true);
        when(userRepository.existsByEmail("nope@test.com")).thenReturn(false);

        assertThat(userService.existsByEmail("exists@test.com")).isTrue();
        assertThat(userService.existsByEmail("nope@test.com")).isFalse();
    }
}
