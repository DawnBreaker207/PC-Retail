package com.dawn.order.statemachine;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.order.entity.enums.SubOrderStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.dawn.order.entity.enums.SubOrderStatus.*;

@Component
public class SubOrderStateMachine {

    private static final Map<SubOrderStatus, Set<SubOrderStatus>> TRANSITIONS = new EnumMap<>(SubOrderStatus.class);

    static {
        TRANSITIONS.put(PENDING_PAYMENT, EnumSet.of(PAID, CANCELLED, EXPIRED));
        TRANSITIONS.put(PAID, EnumSet.of(PROCESSING, CANCELLED));
        TRANSITIONS.put(PROCESSING, EnumSet.of(SHIPPED, READY_FOR_PICKUP, CANCELLED));
        TRANSITIONS.put(READY_FOR_PICKUP, EnumSet.of(DELIVERED));
        TRANSITIONS.put(SHIPPED, EnumSet.of(DELIVERED));
        TRANSITIONS.put(DELIVERED, EnumSet.of(RETURN_REQUESTED));
        TRANSITIONS.put(RETURN_REQUESTED, EnumSet.of(RETURNED, DELIVERED));
        TRANSITIONS.put(RETURNED, EnumSet.noneOf(SubOrderStatus.class));
        TRANSITIONS.put(CANCELLED, EnumSet.noneOf(SubOrderStatus.class));
        TRANSITIONS.put(EXPIRED, EnumSet.noneOf(SubOrderStatus.class));
    }


    public void assertValidTransition(SubOrderStatus from, SubOrderStatus to) {
        Set<SubOrderStatus> allowed = TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new ConflictException("Invalid transition from " + from + " -> " + to);
        }
    }
}
