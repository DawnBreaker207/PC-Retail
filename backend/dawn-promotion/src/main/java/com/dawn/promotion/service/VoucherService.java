package com.dawn.promotion.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.exception.wrapper.ResourceAlreadyExistedException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.response.ResponsePage;
import com.dawn.promotion.controller.request.VoucherCreateRequest;
import com.dawn.promotion.controller.request.VoucherValidateRequest;
import com.dawn.promotion.dto.VoucherDto;
import com.dawn.promotion.dto.VoucherValidationResult;
import com.dawn.promotion.entity.Voucher;
import com.dawn.promotion.entity.enums.DiscountType;
import com.dawn.promotion.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class VoucherService {
    private static final String VOUCHER_KEY = "voucher:remaining";
    private final VoucherRepository voucherRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public VoucherDto create(VoucherCreateRequest req) {
        if (voucherRepository.existsByCodeAndDeletedAtIsNull(req.code())) {
            throw new ResourceAlreadyExistedException("Voucher code already existed");
        }
        if (req.validFrom().isAfter(req.validTo())) {
            throw new InvalidRequestException("validFrom must be before validTo");
        }

        Voucher voucher = Voucher.builder()
                .code(req.code().toUpperCase())
                .scopeType(req.scopeType())
                .scopeId(req.scopeId())
                .discountType(req.discountType())
                .discountValue(req.discountValue())
                .maxDiscountAmount(req.maxDiscountAmount())
                .minOrderValue(req.minOrderValue() != null
                        ? req.minOrderValue() : BigDecimal.ZERO)
                .totalQuantity(req.totalQuantity())
                .remainingQuantity(req.totalQuantity())
                .validFrom(req.validFrom())
                .validTo(req.validTo())
                .build();

        voucherRepository.save(voucher);

        // Sync remaining quantity into Redis
        redisTemplate.opsForValue().set(
                VOUCHER_KEY + voucher.getId(),
                String.valueOf(voucher.getRemainingQuantity()));

        return toDto(voucher);
    }

    @Transactional(readOnly = true)
    public ResponsePage<VoucherDto> getAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponsePage.of(voucherRepository.findAllByDeletedAtIsNull(pageable).map(this::toDto));
    }

    @Transactional
    public void delete(Long id) {
        Voucher voucher = voucherRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));

        voucher.setDeletedAt(Instant.now());
        voucherRepository.save(voucher);
        redisTemplate.delete(VOUCHER_KEY + id);
    }

    @Transactional(readOnly = true)
    public VoucherValidationResult validate(String code, BigDecimal orderTotal) {
        Voucher voucher = voucherRepository
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

    @Transactional(readOnly = true)
    public BigDecimal calculateDiscountByCode(String code, BigDecimal orderTotal) {
        Voucher voucher = voucherRepository
                .findByCodeAndDeletedAtIsNull(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));

        return calculateDiscount(voucher, orderTotal);
    }

    @Transactional(readOnly = true)
    public Voucher getByCode(String code) {
        return voucherRepository
                .findByCodeAndDeletedAtIsNull(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
    }

    // Private Helpers

    private int getRemainingFromCache(Voucher voucher) {
        String cached = redisTemplate.opsForValue().get(VOUCHER_KEY + voucher.getId());
        if (cached != null) return Integer.parseInt(cached);
        return voucher.getRemainingQuantity();
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
