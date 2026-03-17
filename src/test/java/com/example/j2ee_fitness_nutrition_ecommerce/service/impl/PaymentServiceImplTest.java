package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Payment;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.PaymentMethod;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.PaymentStatus;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @InjectMocks private PaymentServiceImpl paymentService;

    @Test
    void createPayment_COD_autoCompleted() {
        Order order = Order.builder().id(1L).build();
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        Payment result = paymentService.createPayment(order, PaymentMethod.COD);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(result.getPaidAt()).isNotNull();
        assertThat(result.getTransactionCode()).startsWith("PAY-");
    }

    @Test
    void createPayment_bankTransfer_pendingStatus() {
        Order order = Order.builder().id(1L).build();
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        Payment result = paymentService.createPayment(order, PaymentMethod.BANK_TRANSFER);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getPaidAt()).isNull();
    }

    @Test
    void confirmPayment_setsCompletedAndPaidAt() {
        Payment payment = Payment.builder().id(1L).paymentStatus(PaymentStatus.PENDING).build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.confirmPayment(1L);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(result.getPaidAt()).isNotNull();
    }

    @Test
    void failPayment_setsFailedStatus() {
        Payment payment = Payment.builder().id(1L).paymentStatus(PaymentStatus.PENDING).build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.failPayment(1L);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.getPaidAt()).isNull();
    }

    @Test
    void confirmPayment_notFound_throwsException() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.confirmPayment(99L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
