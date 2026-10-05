package com.dawn.rma.service;

import com.dawn.common.auditlog.LogConstant;
import com.dawn.common.auditlog.Loggable;
import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.rma.entity.Refund;
import com.dawn.rma.entity.RefundStatus;
import com.dawn.rma.entity.ReturnRequest;
import com.dawn.rma.entity.ReturnStatus;
import com.dawn.rma.infra.persistence.JpaReturnRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnService {

    private final JpaReturnRequestRepository returnRepo;
    private final com.dawn.rma.infra.persistence.JpaRefundRepository refundRepo;
    private final com.dawn.order.dto.OrderQueryPort orderQueryPort;

    @Transactional
    public ReturnRequest request(Long subOrderId, Long userId, String reason, List<String> images) {
        if (reason == null || reason.isBlank()) {
            throw new com.dawn.common.exception.wrapper.InvalidRequestException("Reason is required");
        }
        if (!orderQueryPort.isSubOrderDelivered(subOrderId)) {
            throw new com.dawn.common.exception.wrapper.InvalidRequestException(
                    "Return is allowed only after delivery");
        }
        boolean activeExists = returnRepo.findBySubOrderId(subOrderId).stream()
                .anyMatch(r -> r.getStatus() == ReturnStatus.REQUESTED
                        || r.getStatus() == ReturnStatus.APPROVED
                        || r.getStatus() == ReturnStatus.DISPUTED);
        if (activeExists) {
            throw new ConflictException("An active return request already exists for sub-order " + subOrderId);
        }
        ReturnRequest r = ReturnRequest.builder()
                .subOrderId(subOrderId)
                .requestedBy(userId)
                .reason(reason)
                .images(images == null ? List.of() : images)
                .status(ReturnStatus.REQUESTED)
                .build();
        return returnRepo.save(r);
    }

    @Transactional(readOnly = true)
    public ReturnRequest get(Long id) {
        return returnRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnRequest not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ReturnRequest> staffList(String status) {
        List<ReturnRequest> all = returnRepo.findAll();
        if (status == null || status.isBlank()) return all;
        final ReturnStatus s;
        try {
            s = ReturnStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return all;
        }
        return all.stream().filter(r -> r.getStatus() == s).toList();
    }

    @Transactional
    public ReturnRequest dispute(Long id, Long userId, String note) {
        ReturnRequest r = get(id);
        if (r.getStatus() != ReturnStatus.REJECTED) {
            throw new ConflictException("Only REJECTED requests can be disputed, current: " + r.getStatus());
        }
        if (!r.getRequestedBy().equals(userId)) {
            throw new ConflictException("Only the requester can dispute");
        }
        r.setStatus(ReturnStatus.DISPUTED);
        r.setDisputedAt(Instant.now());
        r.setDisputeNote(note);
        return returnRepo.save(r);
    }

    @Transactional
    public ReturnRequest resolveDispute(Long id, Long adminId, boolean overturn, String note) {
        ReturnRequest r = get(id);
        if (r.getStatus() != ReturnStatus.DISPUTED) {
            throw new ConflictException("Only DISPUTED requests can be resolved, current: " + r.getStatus());
        }
        r.setDisputeResolvedBy(adminId);
        r.setDisputeResolution(overturn
                ? com.dawn.rma.entity.DisputeResolution.OVERTURN_APPROVE
                : com.dawn.rma.entity.DisputeResolution.UPHOLD_REJECT);
        r.setDisputeNote(note);
        r.setStatus(overturn ? ReturnStatus.APPROVED : ReturnStatus.REJECTED);
        r.setReviewedBy(adminId);
        r.setReviewedAt(Instant.now());
        ReturnRequest saved = returnRepo.save(r);
        if (overturn) {
            refundRepo.save(Refund.builder()
                    .returnRequestId(saved.getId())
                    .subOrderId(saved.getSubOrderId())
                    .amount(BigDecimal.ZERO)
                    .refundMethod("BANK_TRANSFER_MANUAL")
                    .status(RefundStatus.PENDING)
                    .build());
        }
        return saved;
    }

    @Transactional
    @Loggable(action = LogConstant.Action.APPROVE, entity = LogConstant.Entity.RETURN_REQUEST, tier = 1)
    public ReturnRequest approve(Long id, Long staffId, BigDecimal amount, String method) {
        ReturnRequest r = get(id);
        if (r.getStatus() != ReturnStatus.REQUESTED && r.getStatus() != ReturnStatus.DISPUTED) {
            throw new ConflictException("Invalid transition from " + r.getStatus() + " -> APPROVED");
        }
        r.setStatus(ReturnStatus.APPROVED);
        r.setReviewedBy(staffId);
        r.setReviewedAt(Instant.now());
        returnRepo.save(r);
        // ponytail: refund tách riêng để retry độc lập khi gateway fail
        refundRepo.save(Refund.builder()
                .returnRequestId(r.getId())
                .subOrderId(r.getSubOrderId())
                .amount(amount == null ? BigDecimal.ZERO : amount)
                .refundMethod(method == null ? "BANK_TRANSFER_MANUAL" : method)
                .status(RefundStatus.PENDING)
                .build());
        return r;
    }

    @Transactional
    @Loggable(action = LogConstant.Action.REJECT, entity = LogConstant.Entity.RETURN_REQUEST, tier = 1)
    public ReturnRequest reject(Long id, Long staffId) {
        ReturnRequest r = get(id);
        if (r.getStatus() != ReturnStatus.REQUESTED) {
            throw new ConflictException("Invalid transition from " + r.getStatus() + " -> REJECTED");
        }
        r.setStatus(ReturnStatus.REJECTED);
        r.setReviewedBy(staffId);
        r.setReviewedAt(Instant.now());
        return returnRepo.save(r);
    }
}
