package com.example.j2ee_fitness_nutrition_ecommerce.repository;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user = User.builder()
                .fullName("Test User").email("test@test.com")
                .password("encoded").role(Role.USER).build();
        userRepository.save(user);
    }

    @Test
    void findByEmail_existingUser_returnsUser() {
        Optional<User> result = userRepository.findByEmail("test@test.com");
        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("Test User");
    }

    @Test
    void findByEmail_nonExisting_returnsEmpty() {
        Optional<User> result = userRepository.findByEmail("nobody@test.com");
        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmail_returnsCorrectBoolean() {
        assertThat(userRepository.existsByEmail("test@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("nobody@test.com")).isFalse();
    }
}
