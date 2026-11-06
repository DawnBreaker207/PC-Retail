package com.dawn.order.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.order.dto.ReturnRequestDto;
import com.dawn.order.entity.Refund;
import com.dawn.order.entity.ReturnRequest;
import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.RefundMethod;
import com.dawn.order.entity.enums.RefundStatus;
import com.dawn.order.entity.enums.ReturnStatus;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.order.port.out.RefundRepository;
import com.dawn.order.port.out.ReturnRequestRepository;
import com.dawn.order.port.out.SubOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnRequestService {

    private static final int RETURN_WINDOW_DAYS = 7;

    private final ReturnRequestRepository returnRequestRepo;
    private final RefundRepository refundRepo;
    private final SubOrderRepository subOrderRepo;

    @Transactional
    public ReturnRequestDto submit(Long subOrderId, Long userId, String reason, List<String> images) {
        SubOrder subOrder = subOrderRepo.findById(subOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Message.format(Message.Order.SUB_ORDER_NOT_FOUND, subOrderId)));

        if (subOrder.getStatus() != SubOrderStatus.DELIVERED) {
            throw new InvalidRequestException(Message.Order.RETURN_ONLY_AFTER_DELIVERY);
        }

        Instant deliveredAt = subOrder.getStatusHistory().stream()
                .filter(h -> SubOrderStatus.DELIVERED.name().equals(h.get("to")))
                .map(h -> Instant.parse((String) h.get("createdAt")))
                .max(Instant::compareTo)
                .orElse(subOrder.getCreatedAt());

        if (Instant.now().isAfter(deliveredAt.plus(RETURN_WINDOW_DAYS, ChronoUnit.DAYS))) {
            throw new InvalidRequestException(Message.Order.RETURN_WINDOW_CLOSED);
        }

        boolean hasOpenRequest = returnRequestRepo.findBySubOrderIdAndStatusIn(
                subOrderId, List.of(ReturnStatus.REQUESTED, ReturnStatus.APPROVED)).isPresent();
        if (hasOpenRequest) {
            throw new ConflictException(Message.Order.RETURN_ALREADY_OPEN);
        }

        int claimed = subOrderRepo.transitionIfStatus(subOrderId, SubOrderStatus.DELIVERED, SubOrderStatus.RETURN_REQUESTED);
        if (claimed == 0) {
            throw new ConflictException(Message.format(Message.Order.INVALID_TRANSITION, SubOrderStatus.DELIVERED, SubOrderStatus.RETURN_REQUESTED));
        }

        ReturnRequest request = ReturnRequest.builder()
                .subOrderId(subOrderId)
                .requestedBy(userId)
                .reason(reason)
                .images(images != null ? images : List.of())
                .status(ReturnStatus.REQUESTED)
                .build();

        return toDto(returnRequestRepo.save(request));
    }

    @Transactional
    public ReturnRequestDto review(Long returnRequestId, Long staffId, boolean approve, String note) {
        ReturnRequest request = findOrThrow(returnRequestId);

        if (request.getStatus() != ReturnStatus.REQUESTED) {
            throw new ConflictException(Message.Order.RETURN_ALREADY_REVIEWED);
        }

        request.setStatus(approve ? ReturnStatus.APPROVED : ReturnStatus.REJECTED);
        request.setReviewedBy(staffId);
        request.setReviewedAt(Instant.now());

        if (!approve) {
            request.setDisputeNote(note);
        } else {
            initiateRefund(request.getSubOrderId(), returnRequestId);
        }

        return toDto(returnRequestRepo.save(request));
    }

    @Transactional
    public ReturnRequestDto dispute(Long returnRequestId, Long userId) {
        ReturnRequest request = findOrThrow(returnRequestId);

        if (request.getStatus() != ReturnStatus.REJECTED) {
            throw new ConflictException(Message.Order.RETURN_DISPUTE_NOT_ALLOWED);
        }
        if (!userId.equals(request.getRequestedBy())) {
            throw new InvalidRequestException(Message.Auth.FORBIDDEN);
        }

        request.setStatus(ReturnStatus.DISPUTED);
        request.setDisputedAt(Instant.now());
        return toDto(returnRequestRepo.save(request));
    }

    @Transactional
    public ReturnRequestDto resolveDispute(Long returnRequestId, Long staffId,
                                           boolean overturn, String note) {
        ReturnRequest request = findOrThrow(returnRequestId);

        if (request.getStatus() != ReturnStatus.DISPUTED) {
            throw new ConflictException(Message.Order.RETURN_NOT_IN_DISPUTE);
        }

        request.setDisputeResolvedBy(staffId);
        request.setDisputeResolution(overturn ? "OVERTURN_APPROVE" : "UPHOLD_REJECT");
        request.setDisputeNote(note);

        if (overturn) {
            request.setStatus(ReturnStatus.APPROVED);
            initiateRefund(request.getSubOrderId(), returnRequestId);
        } else {
            request.setStatus(ReturnStatus.REJECTED);
            subOrderRepo.transitionIfStatus(
                    request.getSubOrderId(), SubOrderStatus.RETURN_REQUESTED, SubOrderStatus.DELIVERED);
        }

        return toDto(returnRequestRepo.save(request));
    }

    public ReturnRequestDto getById(Long id) {
        return toDto(findOrThrow(id));
    }

    private void initiateRefund(Long subOrderId, Long returnRequestId) {
        SubOrder subOrder = subOrderRepo.findById(subOrderId).orElseThrow();
        BigDecimal amount = subOrder.getFinalAmount();
        Refund refund = Refund.builder()
                .returnRequestId(returnRequestId)
                .subOrderId(subOrderId)
                .amount(amount)
                .status(RefundStatus.PENDING)
                .refundMethod(RefundMethod.BANK_TRANSFER)
                .build();
        refundRepo.save(refund);
        log.info("Refund initiated for subOrder={} amount={}", subOrderId, amount);
    }

    private ReturnRequest findOrThrow(Long id) {
        return returnRequestRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Message.format(Message.Order.RETURN_REQUEST_NOT_FOUND, id)));
    }

    private ReturnRequestDto toDto(ReturnRequest r) {
        return new ReturnRequestDto(r.getId(), r.getSubOrderId(), r.getReason(),
                r.getStatus(), r.getImages(), r.getCreatedAt(), r.getReviewedAt());
    }
}
