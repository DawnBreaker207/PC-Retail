package com.dawn.order.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.order.statemachine.SubOrderStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.security.core.parameters.P;

import static com.dawn.order.entity.enums.SubOrderStatus.*;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SubOrderStateMachineTest {
    private SubOrderStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new SubOrderStateMachine();
    }

    // Valid Transitions
    @Nested
    @DisplayName("Valid transitions — must not throw")
    class ValidTransitions {
        @Test
        void pendingPayment_to_paid() {
            assertThatNoException().isThrownBy(() ->
                    stateMachine.assertValidTransition(PENDING_PAYMENT, PAID));
        }

        @Test
        void pendingPayment_to_cancelled() {
            assertThatNoException().isThrownBy(() ->
                    stateMachine.assertValidTransition(PENDING_PAYMENT, CANCELLED));
        }


        @Test
        void pendingPayment_to_expired() {
            assertThatNoException().isThrownBy(() ->
                    stateMachine.assertValidTransition(PENDING_PAYMENT, EXPIRED));
        }

        @Test
        void paid_to_processing() {
            assertThatNoException().isThrownBy(() ->
                    stateMachine.assertValidTransition(PAID, PROCESSING));
        }

        @Test
        void paid_to_cancelled() {
            assertThatNoException().isThrownBy(() ->
                    stateMachine.assertValidTransition(PAID, CANCELLED));
        }

        @Test
        void processing_to_shipped() {
            assertThatNoException().isThrownBy(() ->
                    stateMachine.assertValidTransition(PROCESSING, SHIPPED));
        }

        @Test
        void processing_to_readyForPickUp() {
            assertThatNoException().isThrownBy(() ->
                    stateMachine.assertValidTransition(PROCESSING, READY_FOR_PICKUP));
        }

        @Test
        void shipped_to_delivered() {
            assertThatNoException().isThrownBy(() ->
                    stateMachine.assertValidTransition(SHIPPED, DELIVERED));
        }

        @Test
        void readyToPickup_to_delivered() {
            assertThatNoException().isThrownBy(() ->
                    stateMachine.assertValidTransition(READY_FOR_PICKUP, DELIVERED));
        }

        @Test
        void delivered_to_returnRequested() {
            assertThatNoException().isThrownBy(() ->
                    stateMachine.assertValidTransition(DELIVERED, RETURN_REQUESTED));
        }

        @Test
        void returnRequested_to_returned() {
            assertThatNoException().isThrownBy(() ->
                    stateMachine.assertValidTransition(RETURN_REQUESTED, RETURNED));
        }

        @Test
        @DisplayName("Admin rejects return — RETURN_REQUESTED back to DELIVERED")
        void returnRequested_to_delivered() {
            assertThatNoException().isThrownBy(() ->
                    stateMachine.assertValidTransition(RETURN_REQUESTED, DELIVERED));
        }
    }

    // Terminal State
    @Nested
    @DisplayName("Terminal State — all outgoing transitions must be rejected")
    class TerminalState {
        @ParameterizedTest(name = "EXPIRED -> {0} must throw")
        @EnumSource(SubOrderStatus.class)
        void expired_rejectsAllTransitions(SubOrderStatus to) {
            assertThatThrownBy(() -> stateMachine.assertValidTransition(EXPIRED, to))
                    .isInstanceOf(ConflictException.class);
        }


        @ParameterizedTest(name = "CANCELLED -> {0} must throw")
        @EnumSource(SubOrderStatus.class)
        void cancelled_rejectsAllTransitions(SubOrderStatus to) {
            assertThatThrownBy(() -> stateMachine.assertValidTransition(CANCELLED, to))
                    .isInstanceOf(ConflictException.class);
        }

        @ParameterizedTest(name = "RETURNED -> {0} must throw")
        @EnumSource(SubOrderStatus.class)
        void returned_rejectsAllTransitions(SubOrderStatus to) {
            assertThatThrownBy(() -> stateMachine.assertValidTransition(RETURNED, to))
                    .isInstanceOf(ConflictException.class);
        }
    }

    // Invalid Transitions
    @Nested
    @DisplayName("Invalid Transitions — common incorrect jumps must be rejected")
    class InvalidTransitions {

        @Test
        @DisplayName("Cannot reverse CANCELLED -> DELIVERED")
        void cancelled_to_delivered_isRejected() {
            assertThatThrownBy(() -> stateMachine.assertValidTransition(CANCELLED, DELIVERED))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        @DisplayName("Cannot skip PENDING_PAYMENT -> PROCESSING")
        void pendingPayment_to_processing_isRejected() {
            assertThatThrownBy(() -> stateMachine.assertValidTransition(PENDING_PAYMENT, PROCESSING))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        @DisplayName("Cannot skip PENDING_PAYMENT -> DELIVERED")
        void pendingPayment_to_delivered_isRejected() {
            assertThatThrownBy(() -> stateMachine.assertValidTransition(PENDING_PAYMENT, DELIVERED))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        @DisplayName("Cannot go backward SHIPPED -> PROCESSING")
        void shipped_to_processing_isRejected() {
            assertThatThrownBy(() -> stateMachine.assertValidTransition(SHIPPED, PROCESSING))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        @DisplayName("Cannot go backward DELIVERED -> PAID")
        void delivered_to_paid_isRejected() {
            assertThatThrownBy(() -> stateMachine.assertValidTransition(DELIVERED, PAID))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        @DisplayName("Cannot skip PROCESSING -> DELIVERED — must go through SHIPPED or READY_FOR_PICKUP")
        void processing_to_delivered_isRejected() {
            assertThatThrownBy(() -> stateMachine.assertValidTransition(PROCESSING, DELIVERED))
                    .isInstanceOf(ConflictException.class);
        }
    }
}
