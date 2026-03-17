package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Coupon;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.DiscountType;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock private CouponRepository couponRepository;
    @InjectMocks private CouponServiceImpl couponService;

    @Test
    void validate_validCoupon_returnsCoupon() {
        Coupon coupon = Coupon.builder()
                .id(1L).code("SAVE10").active(true)
                .discountType(DiscountType.PERCENTAGE).discountValue(new BigDecimal("10"))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .maxUses(100).currentUses(5)
                .build();

        when(couponRepository.findByCodeAndActiveTrue("SAVE10")).thenReturn(Optional.of(coupon));

        Coupon result = couponService.validate("SAVE10", new BigDecimal("500000"));

        assertThat(result.getCode()).isEqualTo("SAVE10");
    }

    @Test
    void validate_invalidCode_throwsException() {
        when(couponRepository.findByCodeAndActiveTrue("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.validate("INVALID", new BigDecimal("100000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid coupon code");
    }

    @Test
    void validate_expiredCoupon_throwsException() {
        Coupon coupon = Coupon.builder()
                .id(1L).code("EXPIRED").active(true)
                .endDate(LocalDateTime.now().minusDays(1))
                .build();

        when(couponRepository.findByCodeAndActiveTrue("EXPIRED")).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.validate("EXPIRED", new BigDecimal("100000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void validate_notYetActive_throwsException() {
        Coupon coupon = Coupon.builder()
                .id(1L).code("FUTURE").active(true)
                .startDate(LocalDateTime.now().plusDays(5))
                .build();

        when(couponRepository.findByCodeAndActiveTrue("FUTURE")).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.validate("FUTURE", new BigDecimal("100000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not yet active");
    }

    @Test
    void validate_usageLimitReached_throwsException() {
        Coupon coupon = Coupon.builder()
                .id(1L).code("MAXED").active(true)
                .maxUses(10).currentUses(10)
                .build();

        when(couponRepository.findByCodeAndActiveTrue("MAXED")).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.validate("MAXED", new BigDecimal("100000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("usage limit");
    }

    @Test
    void validate_belowMinOrderAmount_throwsException() {
        Coupon coupon = Coupon.builder()
                .id(1L).code("MIN500").active(true)
                .minOrderAmount(new BigDecimal("500000"))
                .build();

        when(couponRepository.findByCodeAndActiveTrue("MIN500")).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.validate("MIN500", new BigDecimal("200000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Minimum order amount");
    }

    @Test
    void calculateDiscount_percentage() {
        Coupon coupon = Coupon.builder()
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .build();

        BigDecimal discount = couponService.calculateDiscount(coupon, new BigDecimal("500000"));

        assertThat(discount).isEqualByComparingTo("50000");
    }

    @Test
    void calculateDiscount_fixed() {
        Coupon coupon = Coupon.builder()
                .discountType(DiscountType.FIXED)
                .discountValue(new BigDecimal("75000"))
                .build();

        BigDecimal discount = couponService.calculateDiscount(coupon, new BigDecimal("500000"));

        assertThat(discount).isEqualByComparingTo("75000");
    }

    @Test
    void calculateDiscount_fixedCappedAtOrderTotal() {
        Coupon coupon = Coupon.builder()
                .discountType(DiscountType.FIXED)
                .discountValue(new BigDecimal("200000"))
                .build();

        BigDecimal discount = couponService.calculateDiscount(coupon, new BigDecimal("100000"));

        assertThat(discount).isEqualByComparingTo("100000");
    }

    @Test
    void incrementUsage_incrementsByOne() {
        Coupon coupon = Coupon.builder().id(1L).currentUses(5).build();
        when(couponRepository.save(any(Coupon.class))).thenAnswer(inv -> inv.getArgument(0));

        couponService.incrementUsage(coupon);

        assertThat(coupon.getCurrentUses()).isEqualTo(6);
        verify(couponRepository).save(coupon);
    }

    @Test
    void deleteById_softDelete_setsActiveToFalse() {
        Coupon coupon = Coupon.builder().id(1L).code("DEL").active(true).build();
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponRepository.save(any(Coupon.class))).thenAnswer(inv -> inv.getArgument(0));

        couponService.deleteById(1L);

        assertThat(coupon.isActive()).isFalse();
        verify(couponRepository).save(coupon);
        verify(couponRepository, never()).deleteById(anyLong());
    }
}
