package com.dawn.promotion.service;

import com.dawn.common.auditlog.LogConstant;
import com.dawn.common.auditlog.Loggable;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.common.response.ResponsePage;
import com.dawn.promotion.controller.request.VoucherCreateRequest;
import com.dawn.promotion.dto.VoucherDto;
import com.dawn.promotion.entity.Voucher;
import com.dawn.promotion.port.in.ManageVoucherUseCase;
import com.dawn.promotion.port.out.PromotionCachePort;
import com.dawn.promotion.port.out.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PromotionService implements ManageVoucherUseCase {

    private final VoucherRepository voucherRepo;
    private final PromotionCachePort cachePort;

    @Transactional(readOnly = true)
    @Override
    public ResponsePage<VoucherDto> getAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponsePage.of(voucherRepo.findAllByDeletedAtIsNull(pageable).map(this::toDto));
    }


    @Override
    @Transactional
    @Loggable(action = LogConstant.Action.CREATE, entity = LogConstant.Entity.VOUCHER)
    public VoucherDto create(VoucherCreateRequest req) {
        String code = req.code().toUpperCase();
        if (voucherRepo.existsByCodeAndDeletedAtIsNull(code)) {
            throw new ConflictException(Message.Promotion.VOUCHER_CODE_EXISTS);
        }
        if (req.validFrom().isAfter(req.validTo())) {
            throw new InvalidRequestException(Message.Promotion.VOUCHER_DATE_INVALID);
        }

        Voucher voucher = Voucher.builder()
                .code(code)
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

        voucherRepo.save(voucher);

        // Sync remaining quantity into Redis
        cachePort.setVoucherRemaining(code, voucher.getRemainingQuantity());

        return toDto(voucher);
    }


    @Transactional
    @Override
    @Loggable(action = LogConstant.Action.DELETE, entity = LogConstant.Entity.VOUCHER)
    public void delete(Long id) {
        Voucher voucher = voucherRepo
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Message.Promotion.VOUCHER_NOT_FOUND));

        voucher.setDeletedAt(Instant.now());
        voucherRepo.save(voucher);
        cachePort.deleteVoucher(voucher.getCode());
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
