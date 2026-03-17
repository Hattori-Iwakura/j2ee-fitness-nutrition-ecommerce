package com.example.j2ee_fitness_nutrition_ecommerce.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class CheckoutRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void blankFullName_hasViolation() {
        CheckoutRequest req = validRequest();
        req.setFullName("");
        Set<ConstraintViolation<CheckoutRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fullName"));
    }

    @Test
    void blankPhone_hasViolation() {
        CheckoutRequest req = validRequest();
        req.setPhone("");
        Set<ConstraintViolation<CheckoutRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("phone"));
    }

    @Test
    void blankAddress_hasViolation() {
        CheckoutRequest req = validRequest();
        req.setAddress("");
        Set<ConstraintViolation<CheckoutRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("address"));
    }

    private CheckoutRequest validRequest() {
        CheckoutRequest req = new CheckoutRequest();
        req.setFullName("John Doe");
        req.setPhone("0123456789");
        req.setAddress("123 Main St");
        req.setPaymentMethod("COD");
        return req;
    }
}
