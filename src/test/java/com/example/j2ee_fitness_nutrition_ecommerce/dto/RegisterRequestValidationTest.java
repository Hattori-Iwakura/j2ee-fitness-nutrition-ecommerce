package com.example.j2ee_fitness_nutrition_ecommerce.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class RegisterRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void blankFullName_hasViolation() {
        RegisterRequest req = validRequest();
        req.setFullName("");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fullName"));
    }

    @Test
    void invalidEmail_hasViolation() {
        RegisterRequest req = validRequest();
        req.setEmail("not-an-email");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void shortPassword_hasViolation() {
        RegisterRequest req = validRequest();
        req.setPassword("abc");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void validRequest_noViolations() {
        RegisterRequest req = validRequest();
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    private RegisterRequest validRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("John Doe");
        req.setEmail("john@test.com");
        req.setPassword("password123");
        req.setConfirmPassword("password123");
        req.setPhone("0123456789");
        return req;
    }
}
