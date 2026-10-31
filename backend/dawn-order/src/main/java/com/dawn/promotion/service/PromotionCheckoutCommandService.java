package com.dawn.promotion.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.message.Message;
import com.dawn.promotion.dto.VoucherValidationResult;
import com.dawn.promotion.entity.Voucher;
import com.dawn.promotion.entity.VoucherUsage;
import com.dawn.promotion.entity.enums.DiscountType;
import com.dawn.promotion.entity.enums.ScopeType;
import com.dawn.promotion.port.out.PromotionCachePort;
import com.dawn.promotion.port.out.PromotionCheckoutPort;
import com.dawn.promotion.port.out.VoucherRepository;
import com.dawn.promotion.port.out.VoucherUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionCheckoutCommandService implements PromotionCheckoutPort {

    private final VoucherRepository voucherRepo;
    private final VoucherUsageRepository voucherUsageRepo;
    private final PromotionCachePort cachePort;

    @Override
    public void reservePromotions(List<String> voucherCodes, List<Long> flashSaleSkuIds) {
        if (voucherCodes != null) {
            List<Voucher> vouchers = voucherRepo.findByCodeInAndDeletedAtIsNull(voucherCodes);
            for (Voucher v : vouchers) {
                if (cachePort.getVoucherRemaining(v.getCode()) == null) {
                    cachePort.setVoucherRemaining(v.getCode(), v.getRemainingQuantity());
                }
            }
        }
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
                .anyMatch(v -> v.getScopeType() == ScopeType.STOREWIDE
                        || v.getScopeType() == ScopeType.PRODUCT);

        if (!hasStorewideOrProductVoucher) return;

        boolean cartHasFlashSaleItem = voucherRepo.hasActiveFlashSale(cartSkuIds);
        if (cartHasFlashSaleItem) {
            throw new InvalidRequestException(Message.Promotion.FLASH_SALE_VOUCHER_STACK);
        }
    }

    @Transactional
    @Override
    public void recordUsage(Long userId, Long orderId, List<String> voucherCodes, BigDecimal orderTotal) {
        if (voucherCodes == null || voucherCodes.isEmpty()) return;

        List<Voucher> vouchers = voucherRepo.findWithLockByCodeInAndDeletedAtIsNull(voucherCodes);
        for (Voucher voucher : vouchers) {
            if (voucherUsageRepo.existsByVoucherIdAndUserId(voucher.getId(), userId)) {
                throw new InvalidRequestException(Message.format(Message.Promotion.VOUCHER_ALREADY_USED, voucher.getCode()));
            }
        }

        for (Voucher voucher : vouchers) {
            try {
                voucherUsageRepo.save(VoucherUsage.builder()
                        .voucherId(voucher.getId())
                        .userId(userId)
                        .orderId(orderId)
                        .discountAmount(calculateDiscount(voucher, orderTotal))
                        .build());
            } catch (org.springframework.dao.DataIntegrityViolationException dup) {
                throw new InvalidRequestException(Message.format(Message.Promotion.VOUCHER_ALREADY_USED, voucher.getCode()));
            }
            voucher.setRemainingQuantity(voucher.getRemainingQuantity() - 1);
            voucherRepo.save(voucher);
        }
    }

    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderTotal) {
        BigDecimal discount;
        if (voucher.getDiscountType() == DiscountType.FIXED) {
            discount = voucher.getDiscountValue();
        } else {
            discount = orderTotal
                    .multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (voucher.getMaxDiscountAmount() != null) {
                discount = discount.min(voucher.getMaxDiscountAmount());
            }
        }
        return discount.min(orderTotal);
    }
}
