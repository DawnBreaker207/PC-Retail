package com.dawn.promotion.service;

import com.dawn.common.message.Message;
import com.dawn.common.response.ResponsePage;
import com.dawn.promotion.dto.VoucherDto;
import com.dawn.promotion.dto.VoucherValidationResult;
import com.dawn.promotion.entity.Voucher;
import com.dawn.promotion.entity.enums.DiscountType;
import com.dawn.promotion.port.in.ValidatePromotionUseCase;
import com.dawn.promotion.port.out.PromotionCachePort;
import com.dawn.promotion.port.out.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PromotionQueryService implements ValidatePromotionUseCase {

    private final VoucherRepository voucherRepo;
    private final PromotionCachePort cachePort;

    @Transactional(readOnly = true)
    public ResponsePage<VoucherDto> getAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponsePage.of(voucherRepo.findAllByDeletedAtIsNull(pageable).map(this::toDto));
    }

    @Transactional(readOnly = true)
    @Override
    public VoucherValidationResult validate(String code, BigDecimal orderTotal) {
        Voucher voucher = voucherRepo
                .findByCodeAndDeletedAtIsNull(code)
                .orElse(null);

        if (voucher == null) {
            return VoucherValidationResult.fail(Message.format(Message.Promotion.VOUCHER_NOT_FOUND, code));
        }

        if (voucher.getRemainingQuantity() <= 0) {
            return VoucherValidationResult.fail(Message.Promotion.VOUCHER_OUT_OF_STOCK);
        }

        Instant now = Instant.now();
        if (now.isBefore(voucher.getValidFrom()) || now.isAfter(voucher.getValidTo())) {
            return VoucherValidationResult.fail(Message.Promotion.VOUCHER_EXPIRED);
        }

        int remaining = getRemainingFromCache(voucher);
        if (remaining <= 0) {
            return VoucherValidationResult.fail(Message.Promotion.VOUCHER_OUT_OF_STOCK);
        }

        if (orderTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            return VoucherValidationResult.fail(Message.format(Message.Promotion.VOUCHER_MIN_ORDER_NOT_MET, voucher.getMinOrderValue()));
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
            if (voucher.getMaxDiscountAmount() != null) {
                discount = discount.min(voucher.getMaxDiscountAmount());
            }
        }
        return discount.min(orderTotal);
    }

    private VoucherDto toDto(Voucher v) {
        return new VoucherDto(
                v.getId(),
                v.getCode(),
                v.getScopeType(),
                v.getScopeId(),
                v.getDiscountType(),
                v.getDiscountValue(),
                v.getMaxDiscountAmount(),
                v.getMinOrderValue(),
                v.getTotalQuantity(),
                v.getRemainingQuantity(),
                v.getValidFrom(),
                v.getValidTo());
    }
}
