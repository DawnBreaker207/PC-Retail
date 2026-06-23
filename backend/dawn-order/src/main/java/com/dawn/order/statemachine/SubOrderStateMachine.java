package com.dawn.order.statemachine;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.order.entity.enums.SubOrderStatus;

import java.util.Map;
import java.util.Set;

public class SubOrderStateMachine {

    private static final Map<SubOrderStatus, Set<SubOrderStatus>> TRANSITIONS = Map.of(
            SubOrderStatus.PENDING_PAYMENT, Set.of(SubOrderStatus.PAID, SubOrderStatus.EXPIRED, SubOrderStatus.CANCELLED),
            SubOrderStatus.PAID, Set.of(SubOrderStatus.PROCESSING, SubOrderStatus.CANCELLED),
            SubOrderStatus.PROCESSING, Set.of(SubOrderStatus.SHIPPED, SubOrderStatus.READY_FOR_PICKUP, SubOrderStatus.CANCELLED),
            SubOrderStatus.SHIPPED, Set.of(SubOrderStatus.DELIVERED),
            SubOrderStatus.READY_FOR_PICKUP, Set.of(SubOrderStatus.DELIVERED, SubOrderStatus.CANCELLED),
            SubOrderStatus.DELIVERED, Set.of(SubOrderStatus.RETURN_REQUESTED),
            SubOrderStatus.RETURN_REQUESTED, Set.of(SubOrderStatus.RETURNED, SubOrderStatus.PAID),
            SubOrderStatus.RETURNED, Set.of(),
            SubOrderStatus.EXPIRED, Set.of(),
            SubOrderStatus.CANCELLED, Set.of());

    public static void validate(SubOrderStatus from, SubOrderStatus to) {
        Set<SubOrderStatus> allowed = TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new InvalidRequestException("Invalid transition: " + from + " -> " + to + ". Allowed: " + allowed);
        }
    }

    public static Set<SubOrderStatus> allowedTransitions(SubOrderStatus from) {
        return TRANSITIONS.getOrDefault(from, Set.of());
    }
}
