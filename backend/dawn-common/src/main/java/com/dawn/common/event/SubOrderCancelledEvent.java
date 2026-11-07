package com.dawn.common.event;

/** Published when a PAID ONLINE sub-order is cancelled — triggers payment refund saga. */
public record SubOrderCancelledEvent(Long subOrderId, Long orderId, String reason) {}
