package com.dawn.order.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.order.controller.request.SubmitWarrantyClaimRequest;
import com.dawn.order.dto.WarrantyClaimDto;
import com.dawn.order.dto.WarrantyItemDto;
import com.dawn.order.entity.OrderItem;
import com.dawn.order.entity.WarrantyClaim;
import com.dawn.order.entity.WarrantyItem;
import com.dawn.order.entity.enums.WarrantyClaimStatus;
import com.dawn.order.event.SubOrderDeliveredEvent;
import com.dawn.order.port.out.WarrantyClaimRepository;
import com.dawn.order.port.out.WarrantyItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarrantyService {

    private static final int DEFAULT_SHOP_WARRANTY_MONTHS = 12;

    private final WarrantyItemRepository warrantyItemRepo;
    private final WarrantyClaimRepository warrantyClaimRepo;

    @Async
    @EventListener
    @Transactional
    public void onDelivered(SubOrderDeliveredEvent event) {
        try {
            Instant activatedAt = Instant.now();
            for (OrderItem item : event.items()) {
                Instant expiresAt = activatedAt.plus(DEFAULT_SHOP_WARRANTY_MONTHS * 30L, ChronoUnit.DAYS);
                WarrantyItem warranty = WarrantyItem.builder()
                        .orderItemId(item.getId())
                        .subOrderId(event.subOrderId())
                        .skuId(item.getSkuId())
                        .shopMonths(DEFAULT_SHOP_WARRANTY_MONTHS)
                        .mfrMonths(0)
                        .activatedAt(activatedAt)
                        .expiresAt(expiresAt)
                        .build();
                warrantyItemRepo.save(warranty);
            }
            log.info("Warranty activated for subOrder={} items={}", event.subOrderId(), event.items().size());
        } catch (Exception e) {
            log.error("Warranty activation failed for subOrder={}", event.subOrderId(), e);
        }
    }

    @Transactional
    public WarrantyClaimDto submitClaim(Long warrantyItemId, SubmitWarrantyClaimRequest req) {
        WarrantyItem item = warrantyItemRepo.findById(warrantyItemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Message.format(Message.Order.WARRANTY_NOT_FOUND, warrantyItemId)));

        if (item.getExpiresAt() != null && Instant.now().isAfter(item.getExpiresAt())) {
            throw new InvalidRequestException(Message.Order.WARRANTY_EXPIRED);
        }

        WarrantyClaim claim = WarrantyClaim.builder()
                .warrantyItemId(warrantyItemId)
                .customerName(req.customerName())
                .customerPhone(req.customerPhone())
                .description(req.description())
                .images(req.images() != null ? req.images() : List.of())
                .handledByBranchId(req.branchId())
                .status(WarrantyClaimStatus.SUBMITTED)
                .build();

        return toClaimDto(warrantyClaimRepo.save(claim));
    }

    @Transactional
    public WarrantyClaimDto updateClaimStatus(Long claimId, WarrantyClaimStatus newStatus,
                                               String diagnosisNote, String resolution, Long staffId) {
        WarrantyClaim claim = warrantyClaimRepo.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Message.format(Message.Order.WARRANTY_CLAIM_NOT_FOUND, claimId)));

        claim.setStatus(newStatus);
        if (diagnosisNote != null) claim.setDiagnosisNote(diagnosisNote);
        if (resolution != null) claim.setResolution(resolution);
        if (newStatus == WarrantyClaimStatus.RETURNED || newStatus == WarrantyClaimStatus.REJECTED) {
            claim.setResolvedAt(Instant.now());
        }
        claim.setUpdatedAt(Instant.now());

        return toClaimDto(warrantyClaimRepo.save(claim));
    }

    public List<WarrantyItemDto> getBySubOrder(Long subOrderId) {
        return warrantyItemRepo.findBySubOrderId(subOrderId).stream()
                .map(this::toItemDto).toList();
    }

    public List<WarrantyClaimDto> getClaimsByItem(Long warrantyItemId) {
        return warrantyClaimRepo.findByWarrantyItemId(warrantyItemId).stream()
                .map(this::toClaimDto).toList();
    }

    private WarrantyItemDto toItemDto(WarrantyItem w) {
        boolean active = w.getActivatedAt() != null
                && (w.getExpiresAt() == null || Instant.now().isBefore(w.getExpiresAt()));
        return new WarrantyItemDto(w.getId(), w.getOrderItemId(), w.getSubOrderId(), w.getSkuId(),
                w.getShopMonths(), w.getMfrMonths(), w.getActivatedAt(), w.getExpiresAt(),
                w.getSerialNumber(), active);
    }

    private WarrantyClaimDto toClaimDto(WarrantyClaim c) {
        return new WarrantyClaimDto(c.getId(), c.getWarrantyItemId(), c.getCustomerName(),
                c.getCustomerPhone(), c.getDescription(), c.getImages(), c.getStatus(),
                c.getDiagnosisNote(), c.getResolution(), c.getCreatedAt(), c.getUpdatedAt());
    }
}
