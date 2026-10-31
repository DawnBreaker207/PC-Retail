package com.dawn.promotion.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.promotion.dto.VoucherValidationResult;
import com.dawn.promotion.entity.Voucher;
import com.dawn.promotion.entity.VoucherUsage;
import com.dawn.promotion.entity.enums.DiscountType;
import com.dawn.promotion.entity.enums.ScopeType;
import com.dawn.promotion.port.out.PromotionCachePort;
import com.dawn.promotion.port.out.VoucherRepository;
import com.dawn.promotion.port.out.VoucherUsageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PromotionCheckoutServiceTest {
    @Mock
    VoucherRepository voucherRepo;

    @Mock
    VoucherUsageRepository voucherUsageRepo;

    @Mock
    PromotionCachePort cachePort;

    @InjectMocks
    PromotionCheckoutCommandService service;


    // validateStack
    @Nested
    @DisplayName("validateStack - Flash Sale stacking rule")
    class ValidateStack {
        @Test
        @DisplayName("STOREWIDE voucher + Flash Sale item in cart -> throw")
        void storewideVoucher_withFlashSaleItem_throws() {
            Voucher voucher = buildVoucher("SALE10", ScopeType.STOREWIDE);
            when(voucherRepo.findByCodeInAndDeletedAtIsNull(anyList())).thenReturn(List.of(voucher));
            when(voucherRepo.hasActiveFlashSale(anyList())).thenReturn(true);

            assertThatThrownBy(() -> service.validateStack(List.of(1L, 2L), List.of("SALE10")))
                    .isInstanceOf(InvalidRequestException.class);
        }

        @Test
        @DisplayName("PRODUCT voucher + Flash Sale item in cart -> throw")
        void productVoucher_withFlashSaleItem_throws() {
            Voucher voucher = buildVoucher("PROD10", ScopeType.PRODUCT);
            when(voucherRepo.findByCodeInAndDeletedAtIsNull(anyList())).thenReturn(List.of(voucher));
            when(voucherRepo.hasActiveFlashSale(anyList())).thenReturn(true);

            assertThatThrownBy(() -> service.validateStack(List.of(1L), List.of("PROD10")))
                    .isInstanceOf(InvalidRequestException.class);
        }

        @Test
        @DisplayName("FREESHIP voucher + Flash Sale item -> allowed to stack")
        void freeshippingVoucher_withFlashSaleItem_allowed() {
            Voucher voucher = buildVoucher("FREESHIP", ScopeType.FREESHIP);
            when(voucherRepo.findByCodeInAndDeletedAtIsNull(anyList())).thenReturn(List.of(voucher));

            assertThatNoException().isThrownBy(() ->
                    service.validateStack(List.of(1L), List.of("FREESHIP")));
        }

        @Test
        @DisplayName("STOREWIDE voucher + no Flash Sale item -> allowed")
        void storewideVoucher_noFlashSaleItem_allowed() {
            Voucher voucher = buildVoucher("SALE10", ScopeType.STOREWIDE);
            when(voucherRepo.findByCodeInAndDeletedAtIsNull(anyList())).thenReturn(List.of(voucher));
            when(voucherRepo.hasActiveFlashSale(anyList())).thenReturn(false);

            assertThatNoException().isThrownBy(() ->
                    service.validateStack(List.of(1L), List.of("SALE10")));
        }

        @Test
        @DisplayName("null voucher codes -> skip validation entirely")
        void nullVoucherCodes_skipsValidation() {
            assertThatNoException().isThrownBy(() ->
                    service.validateStack(List.of(1L), null));
            verifyNoInteractions(voucherRepo);
        }

        @Test
        @DisplayName("empty voucher codes -> skip validation entirely")
        void emptyVoucherCodes_skipsValidation() {
            assertThatNoException().isThrownBy(() ->
                    service.validateStack(List.of(1L), List.of()));
            verifyNoInteractions(voucherRepo);
        }
    }

    // validate
    @Nested
    @DisplayName("validate - Voucher validation")
    class Validate {
        @Test
        @DisplayName("Voucher code not found -> fail")
        void voucherNotFound_returnsFail() {
            when(voucherRepo.findByCodeAndDeletedAtIsNull("NOTEXIST")).thenReturn(Optional.empty());

            VoucherValidationResult result = service.validate("NOTEXIST", BigDecimal.valueOf(500000));

            assertThat(result.valid()).isFalse();
        }

        @Test
        @DisplayName("Expired voucher -> fail")
        void expiredVoucher_returnsFail() {
            Voucher voucher = buildValidVoucher("EXPIRED", ScopeType.STOREWIDE, DiscountType.FIXED, BigDecimal.valueOf(50000));
            voucher.setValidTo(Instant.now().minus(1, ChronoUnit.DAYS));
            when(voucherRepo.findByCodeAndDeletedAtIsNull("EXPIRED")).thenReturn(Optional.of(voucher));

            VoucherValidationResult result = service.validate("EXPIRED", BigDecimal.valueOf(500000));

            assertThat(result.valid()).isFalse();
        }

        @Test
        @DisplayName("Voucher out of stock (Redis = 0) -> fail")
        void outOfStock_returnsFail() {
            Voucher voucher = buildValidVoucher("SALE10", ScopeType.STOREWIDE, DiscountType.FIXED, BigDecimal.valueOf(50000));
            when(voucherRepo.findByCodeAndDeletedAtIsNull("SALE10")).thenReturn(Optional.of(voucher));
            when(cachePort.getVoucherRemaining("SALE10")).thenReturn(0);

            VoucherValidationResult result = service.validate("SALE10", BigDecimal.valueOf(500000));

            assertThat(result.valid()).isFalse();
        }

        @Test
        @DisplayName("Order total below min_order_value -> fail")
        void belowMinOrderValue_returnsFail() {
            Voucher voucher = buildValidVoucher("SALE10", ScopeType.STOREWIDE, DiscountType.FIXED, BigDecimal.valueOf(50000));
            voucher.setMinOrderValue(BigDecimal.valueOf(300000));
            when(voucherRepo.findByCodeAndDeletedAtIsNull("SALE10")).thenReturn(Optional.of(voucher));
            when(cachePort.getVoucherRemaining("SALE10")).thenReturn(10);

            VoucherValidationResult result = service.validate("SALE10", BigDecimal.valueOf(100000));

            assertThat(result.valid()).isFalse();
        }

        @Test
        @DisplayName("Valid FIXED voucher -> returns correct discount amount")
        void validFixedVoucher_returnsCorrectDiscount() {
            Voucher voucher = buildValidVoucher("SALE50K", ScopeType.STOREWIDE, DiscountType.FIXED, BigDecimal.valueOf(50000));
            voucher.setMinOrderValue(BigDecimal.valueOf(200000));
            when(voucherRepo.findByCodeAndDeletedAtIsNull("SALE50K")).thenReturn(Optional.of(voucher));
            when(cachePort.getVoucherRemaining("SALE50K")).thenReturn(5);

            VoucherValidationResult result = service.validate("SALE50K", BigDecimal.valueOf(500000));

            assertThat(result.valid()).isTrue();
            assertThat(result.discountAmount()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        }

        @Test
        @DisplayName("Valid PERCENT voucher -> discount capped at max_discount_amount")
        void validPercentVoucher_discountCappedAtMax() {
            Voucher voucher = buildValidVoucher("PCT20", ScopeType.STOREWIDE, DiscountType.PERCENT, BigDecimal.valueOf(20));
            voucher.setMinOrderValue(BigDecimal.ZERO);
            voucher.setMaxDiscountAmount(BigDecimal.valueOf(100000));
            when(voucherRepo.findByCodeAndDeletedAtIsNull("PCT20")).thenReturn(Optional.of(voucher));
            when(cachePort.getVoucherRemaining("PCT20")).thenReturn(3);

            // 20% of 1,000,000 = 200,000 -> capped at 100,000
            VoucherValidationResult result = service.validate("PCT20", BigDecimal.valueOf(1_000_000));

            assertThat(result.valid()).isTrue();
            assertThat(result.discountAmount()).isEqualByComparingTo(BigDecimal.valueOf(100000));
        }

        @Test
        @DisplayName("Discount must never exceed order total")
        void discount_neverExceedsOrderTotal() {
            Voucher voucher = buildValidVoucher("BIG", ScopeType.STOREWIDE, DiscountType.FIXED, BigDecimal.valueOf(500000));
            voucher.setMinOrderValue(BigDecimal.ZERO);
            when(voucherRepo.findByCodeAndDeletedAtIsNull("BIG")).thenReturn(Optional.of(voucher));
            when(cachePort.getVoucherRemaining("BIG")).thenReturn(1);

            // discount 500k but order total is only 100k
            VoucherValidationResult result = service.validate("BIG", BigDecimal.valueOf(100000));

            assertThat(result.valid()).isTrue();
            assertThat(result.discountAmount()).isEqualByComparingTo(BigDecimal.valueOf(100000));
        }
    }

    // recordUsage
    @Nested
    @DisplayName("recordUsage - VoucherUsage persistence")
    class RecordUsage {
        @Test
        @DisplayName("User applies same voucher twice -> throw")
        void duplicateUsage_throws() {
            Voucher voucher = buildVoucher("SALE10", ScopeType.STOREWIDE);
            voucher.setId(1L);
            when(voucherRepo.findWithLockByCodeInAndDeletedAtIsNull(anyList())).thenReturn(List.of(voucher));
            when(voucherUsageRepo.existsByVoucherIdAndUserId(1L, 99L)).thenReturn(true);

            assertThatThrownBy(() -> service.recordUsage(99L, 1L, List.of("SALE10"), voucher.getDiscountValue()))
                    .isInstanceOf(InvalidRequestException.class);
        }

        @Test
        @DisplayName("First time usage -> saves VoucherUsage and decrements remainingQuantity")
        void firstUsage_savesUsageAndDecrementsQuantity() {
            Voucher voucher = buildVoucher("SALE10", ScopeType.STOREWIDE);
            voucher.setId(1L);
            voucher.setRemainingQuantity(5);
            voucher.setDiscountValue(BigDecimal.valueOf(50000));
            when(voucherRepo.findWithLockByCodeInAndDeletedAtIsNull(anyList())).thenReturn(List.of(voucher));
            when(voucherUsageRepo.existsByVoucherIdAndUserId(1L, 99L)).thenReturn(false);

            service.recordUsage(99L, 1L, List.of("SALE10"), voucher.getDiscountValue());

            verify(voucherRepo).save(argThat(v -> v.getRemainingQuantity() == 4));
            verify(voucherUsageRepo).save(any(VoucherUsage.class));
        }

        @Test
        @DisplayName("Empty voucher codes -> no-op")
        void emptyCodes_doesNothing() {
            service.recordUsage(99L, 1L, List.of(), BigDecimal.ZERO);
            verifyNoInteractions(voucherRepo, voucherUsageRepo);
        }
    }

    // Helpers
    private Voucher buildVoucher(String code, ScopeType scopeType) {
        Voucher v = new Voucher();
        v.setCode(code);
        v.setScopeType(scopeType);
        v.setRemainingQuantity(10);
        v.setValidFrom(Instant.now().minus(1, ChronoUnit.DAYS));
        v.setValidTo(Instant.now().plus(7, ChronoUnit.DAYS));
        v.setMinOrderValue(BigDecimal.ZERO);
        v.setDiscountType(DiscountType.FIXED);
        v.setDiscountValue(BigDecimal.valueOf(50000));
        return v;
    }

    private Voucher buildValidVoucher(String code, ScopeType scopeType, DiscountType discountType, BigDecimal discountValue) {
        Voucher v = buildVoucher(code, scopeType);
        v.setDiscountType(discountType);
        v.setDiscountValue(discountValue);
        v.setMaxDiscountAmount(BigDecimal.valueOf(999_999_999));
        return v;
    }
}
