package com.dawn.promotion.service;

import com.dawn.promotion.dto.PromotionCheckoutPort;
import com.dawn.promotion.dto.VoucherValidationResult;
import com.dawn.promotion.entity.Voucher;
import com.dawn.promotion.entity.VoucherUsage;
import com.dawn.promotion.entity.enums.DiscountType;
import com.dawn.promotion.entity.enums.ScopeType;
import com.dawn.promotion.port.in.ValidatePromotionUseCase;
import com.dawn.promotion.port.out.PromotionCachePort;
import com.dawn.promotion.port.out.VoucherRepositoryPort;
import com.dawn.promotion.port.out.VoucherUsageRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionCheckoutService implements ValidatePromotionUseCase, PromotionCheckoutPort {
    private final VoucherRepositoryPort voucherRepo;
    private final VoucherUsageRepositoryPort voucherUsageRepo;
    private final PromotionCachePort cachePort;

    @Override
    public void reservePromotions(List<String> voucherCodes, List<Long> flashSaleSkuIds) {
        cachePort.decrementPromotions(voucherCodes, flashSaleSkuIds);
    }

    @Override
    public void rollbackPromotions(List<String> voucherCodes, List<Long> flashSaleSkuIds) {
        cachePort.incrementPromotions(voucherCodes, flashSaleSkuIds);
    }

    @Override
    public void validateStack(List<Long> cartSkuIds, List<String> voucherCodes) {
        if (voucherCodes == null || voucherCodes.isEmpty()) return;

        List<Voucher> vouchers = voucherRepo.findByCodeInAndDeletedAtIsNull(voucherCodes);
        boolean hasStorewideOrProductVoucher = vouchers.stream()
                .anyMatch(v -> v.getScopeType() == ScopeType.STOREWIDE || v.getScopeType() == ScopeType.PRODUCT);

        if (!hasStorewideOrProductVoucher) return;

        boolean cartHasFlashSaleItem = voucherRepo.hasActiveFlashSale(cartSkuIds);
        if (cartHasFlashSaleItem) {
            throw new IllegalStateException("Can not apply with voucher for product in Flash Sale");
        }
    }

    @Override
    public void recordUsage(Long userId, Long orderId, List<String> voucherCodes) {
        if (voucherCodes == null || voucherCodes.isEmpty()) return;

        List<Voucher> vouchers = voucherRepo.findByCodeInAndDeletedAtIsNull(voucherCodes);
        for (Voucher voucher : vouchers) {
            if (voucherUsageRepo.existsByVoucherIdAndUserId(voucher.getId(), userId)) {
                throw new IllegalStateException("User has used voucher " + voucher.getCode() + " before");
            }
        }

        for (Voucher voucher : vouchers) {
            voucher.setRemainingQuantity(voucher.getRemainingQuantity() - 1);
            voucherRepo.save(voucher);
            voucherUsageRepo.save(VoucherUsage.builder()
                    .voucherId(voucher.getId())
                    .userId(userId)
                    .orderId(orderId)
                    .discountAmount(voucher.getDiscountValue())
                    .build());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public VoucherValidationResult validate(String code, BigDecimal orderTotal) {
        Voucher voucher = voucherRepo
                .findByCodeAndDeletedAtIsNull(code)
                .orElse(null);

        if (voucher == null) {
            return VoucherValidationResult.fail("Voucher code not existed");
        }

        Instant now = Instant.now();
        if (now.isBefore(voucher.getValidFrom()) || now.isAfter(voucher.getValidTo())) {
            return VoucherValidationResult.fail("Voucher was expired or was not affected");
        }

        // Check remaining quantity - Redis first, fallback DB
        int remaining = getRemainingFromCache(voucher);
        if (remaining <= 0) {
            return VoucherValidationResult.fail("Voucher was out of turns");
        }

        if (orderTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            return VoucherValidationResult.fail("minimum amount must be greater " + voucher.getMinOrderValue() + " to use this voucher");
        }

        BigDecimal discount = calculateDiscount(voucher, orderTotal);
        return VoucherValidationResult.ok(discount);
    }


    private int getRemainingFromCache(Voucher voucher) {
        Integer cached = cachePort.getVoucherRemaining(voucher.getCode());
        return cached != null ? cached : voucher.getRemainingQuantity();
    }

    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderTotal) {
        BigDecimal discount;
        if (voucher.getDiscountType() == DiscountType.FIXED) {
            discount = voucher.getDiscountValue();
        } else {
            discount = orderTotal
                    .multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (voucher.getDiscountValue() != null) {
                discount = discount.min(voucher.getMaxDiscountAmount());
            }
        }
        return discount.min(orderTotal);
    }
}
