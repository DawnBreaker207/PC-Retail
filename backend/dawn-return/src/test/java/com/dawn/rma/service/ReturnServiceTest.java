package com.dawn.rma.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.rma.entity.ReturnRequest;
import com.dawn.rma.entity.ReturnStatus;
import com.dawn.rma.infra.persistence.JpaRefundRepository;
import com.dawn.rma.infra.persistence.JpaReturnRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReturnServiceTest {

    @Mock
    JpaReturnRequestRepository returnRepo;
    @Mock
    JpaRefundRepository refundRepo;
    @Mock
    com.dawn.common.contract.OrderQueryPort orderQueryPort;

    ReturnService service;

    @BeforeEach
    void setUp() {
        service = new ReturnService(returnRepo, refundRepo, orderQueryPort);
        lenient().when(orderQueryPort.isSubOrderDelivered(anyLong())).thenReturn(true);
    }

    private ReturnRequest existing(ReturnStatus status) {
        return ReturnRequest.builder().subOrderId(11L).requestedBy(1L)
                .reason("broken").status(status).build();
    }

    @Test
    @DisplayName("Thiếu reason: 400")
    void blankReason_badRequest() {
        assertThatThrownBy(() -> service.request(11L, 1L, "  ", List.of()))
                .isInstanceOf(InvalidRequestException.class);

        verifyNoInteractions(returnRepo);
    }

    @Test
    @DisplayName("Chưa giao hàng: 400, không tạo request")
    void notDelivered_badRequest() {
        when(orderQueryPort.isSubOrderDelivered(11L)).thenReturn(false);

        assertThatThrownBy(() -> service.request(11L, 1L, "broken", List.of()))
                .isInstanceOf(InvalidRequestException.class);

        verify(returnRepo, never()).save(any());
    }

    @Test
    @DisplayName("Đã có request đang xử lý: 409, không tạo mới")
    void activeExists_conflict() {
        when(returnRepo.findBySubOrderId(11L)).thenReturn(List.of(existing(ReturnStatus.REQUESTED)));

        assertThatThrownBy(() -> service.request(11L, 1L, "broken", List.of()))
                .isInstanceOf(ConflictException.class);

        verify(returnRepo, never()).save(any());
    }

    @Test
    @DisplayName("Request cũ đã REJECTED: cho tạo mới")
    void rejectedBefore_allowsNew() {
        when(returnRepo.findBySubOrderId(11L)).thenReturn(List.of(existing(ReturnStatus.REJECTED)));
        when(returnRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.request(11L, 1L, "still broken", List.of());

        verify(returnRepo).save(any(ReturnRequest.class));
    }

    @Test
    @DisplayName("Approve từ REJECTED (không dispute): 409")
    void approveFromRejected_conflict() {
        when(returnRepo.findById(9L)).thenReturn(Optional.of(existing(ReturnStatus.REJECTED)));

        assertThatThrownBy(() -> service.approve(9L, 2L, null, null))
                .isInstanceOf(ConflictException.class);

        verifyNoInteractions(refundRepo);
    }

    @Nested
    @DisplayName("Dispute — khiếu nại sau REJECTED")
    class Dispute {
        @Test
        @DisplayName("REJECTED + đúng người: chuyển DISPUTED")
        void rejectedByRequester_disputed() {
            ReturnRequest r = existing(ReturnStatus.REJECTED);
            when(returnRepo.findById(5L)).thenReturn(Optional.of(r));
            when(returnRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ReturnRequest out = service.dispute(5L, 1L, "không đồng ý");

            assertThat(out.getStatus()).isEqualTo(ReturnStatus.DISPUTED);
            assertThat(out.getDisputedAt()).isNotNull();
        }

        @Test
        @DisplayName("Người khác khiếu nại: 409")
        void otherUser_conflict() {
            when(returnRepo.findById(5L)).thenReturn(Optional.of(existing(ReturnStatus.REJECTED)));

            assertThatThrownBy(() -> service.dispute(5L, 999L, "x"))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        @DisplayName("Overturn: về APPROVED + sinh refund PENDING")
        void overturn_approvesAndRefunds() {
            ReturnRequest r = existing(ReturnStatus.DISPUTED);
            when(returnRepo.findById(5L)).thenReturn(Optional.of(r));
            when(returnRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ReturnRequest out = service.resolveDispute(5L, 2L, true, "ok");

            assertThat(out.getStatus()).isEqualTo(ReturnStatus.APPROVED);
            verify(refundRepo).save(argThat(ref ->
                    ref.getReturnRequestId() == null
                            || ref.getSubOrderId().equals(11L)));
        }

        @Test
        @DisplayName("Uphold: về REJECTED, không sinh refund")
        void uphold_rejectsWithoutRefund() {
            ReturnRequest r = existing(ReturnStatus.DISPUTED);
            when(returnRepo.findById(5L)).thenReturn(Optional.of(r));
            when(returnRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ReturnRequest out = service.resolveDispute(5L, 2L, false, "giữ nguyên");

            assertThat(out.getStatus()).isEqualTo(ReturnStatus.REJECTED);
            verifyNoInteractions(refundRepo);
        }
    }

    @Nested
    @DisplayName("Governance — audit Tier 1 bắt buộc cho approve/reject (docs)")
    class AuditTier {
        @Test
        @DisplayName("approve/reject gắn @Loggable tier=1")
        void approveReject_tier1Logged() throws Exception {
            var approve = ReturnService.class.getMethod("approve", Long.class, Long.class,
                    java.math.BigDecimal.class, String.class);
            var reject = ReturnService.class.getMethod("reject", Long.class, Long.class);

            assertThat(approve.getAnnotation(com.dawn.common.auditlog.Loggable.class).tier()).isOne();
            assertThat(reject.getAnnotation(com.dawn.common.auditlog.Loggable.class).tier()).isOne();
        }
    }
}
