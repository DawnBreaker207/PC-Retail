package com.dawn.notification.infra.messaging;

import com.dawn.common.dlt.DeadLetterService;
import com.dawn.notification.dto.NotificationEvent;
import com.dawn.notification.entity.enums.NotificationChannel;
import com.dawn.notification.infra.remote.UserContactFeignClient;
import com.dawn.notification.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Consumes order-events and payment-events, translates them to notification sends.
 * Keeps notification concerns out of dawn-order and dawn-payment.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventNotificationConsumer {

    private static final String ORDER_TOPIC = "order-events";
    private static final String PAYMENT_TOPIC = "payment-events";

    private final NotificationCommandService notificationCommandService;
    private final UserContactFeignClient userContactClient;
    private final DeadLetterService deadLetterService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = ORDER_TOPIC, groupId = "dawn-notification-order")
    public void onOrderEvent(String payload) {
        if (payload == null || payload.isBlank()) return;
        try {
            Map<String, Object> fields = objectMapper.readValue(payload, new TypeReference<>() {});
            if (fields.containsKey("newStatus")) {
                handleSubOrderStatusChanged(fields);
            } else if (fields.containsKey("returnStatus")) {
                handleReturnStatusChanged(fields);
            } else if (fields.containsKey("orderId") && fields.containsKey("userId")) {
                handleOrderCreated(fields);
            }
        } catch (Exception e) {
            log.error("Failed to process order notification event: {}", e.getMessage());
            deadLetterService.recordFailure(ORDER_TOPIC, null, payload, e.getMessage());
        }
    }

    @KafkaListener(topics = PAYMENT_TOPIC, groupId = "dawn-notification-payment")
    public void onPaymentEvent(String payload) {
        if (payload == null || payload.isBlank()) return;
        try {
            Map<String, Object> fields = objectMapper.readValue(payload, new TypeReference<>() {});
            if (fields.containsKey("paymentId") && fields.containsKey("orderId") && fields.containsKey("amount")) {
                handlePaymentCompleted(fields);
            }
        } catch (Exception e) {
            log.error("Failed to process payment notification event: {}", e.getMessage());
            deadLetterService.recordFailure(PAYMENT_TOPIC, null, payload, e.getMessage());
        }
    }

    // --- handlers ---

    private void handleOrderCreated(Map<String, Object> fields) {
        Long orderId = toLong(fields.get("orderId"));
        Long userId = toLong(fields.get("userId"));
        String email = resolveEmail(userId, null);
        if (email == null) return;

        Map<String, Object> vars = new HashMap<>();
        vars.put("orderId", orderId);
        notificationCommandService.processEvent(new NotificationEvent(
                "OrderCreated", userId, email, null,
                NotificationChannel.EMAIL, "order-created", vars));
    }

    private void handleSubOrderStatusChanged(Map<String, Object> fields) {
        Long subOrderId = toLong(fields.get("subOrderId"));
        Long orderId = toLong(fields.get("orderId"));
        Long userId = toLong(fields.get("userId"));
        String guestEmail = (String) fields.get("guestEmail");
        String newStatus = (String) fields.get("newStatus");
        String trackingCode = (String) fields.get("trackingCode");

        String email = resolveEmail(userId, guestEmail);
        if (email == null) {
            log.warn("No email for subOrder={} newStatus={}, skipping notification", subOrderId, newStatus);
            return;
        }

        String templateId = switch (newStatus != null ? newStatus : "") {
            case "PAID"             -> "payment-success";
            case "SHIPPED"          -> "order-shipped";
            case "OUT_FOR_DELIVERY" -> "delivery-out";
            case "DELIVERY_FAILED"  -> "delivery-failed";
            case "DELIVERED"        -> "order-delivered";
            default -> null;
        };
        if (templateId == null) return;

        Map<String, Object> vars = new HashMap<>();
        vars.put("orderId", orderId);
        vars.put("subOrderId", subOrderId);
        if (trackingCode != null) vars.put("trackingCode", trackingCode);
        // delivery-failed: forward the note if present
        if (fields.containsKey("deliveryNote")) vars.put("deliveryNote", fields.get("deliveryNote"));

        notificationCommandService.processEvent(new NotificationEvent(
                "SubOrderStatusChanged", userId, email, null,
                NotificationChannel.EMAIL, templateId, vars));
    }

    private void handleReturnStatusChanged(Map<String, Object> fields) {
        Long returnRequestId = toLong(fields.get("returnRequestId"));
        Long orderId = toLong(fields.get("orderId"));
        Long userId = toLong(fields.get("userId"));
        String guestEmail = (String) fields.get("guestEmail");
        String returnStatus = (String) fields.get("returnStatus");

        String email = resolveEmail(userId, guestEmail);
        if (email == null) {
            log.warn("No email for returnRequest={} returnStatus={}, skipping", returnRequestId, returnStatus);
            return;
        }

        String templateId = switch (returnStatus != null ? returnStatus : "") {
            case "APPROVED"         -> "return-approved";
            case "REJECTED"         -> "return-rejected";
            case "REFUND_CONFIRMED" -> "refund-confirmed";
            default -> null;
        };
        if (templateId == null) return;

        Map<String, Object> vars = new HashMap<>();
        vars.put("orderId", orderId);
        vars.put("returnRequestId", returnRequestId);
        if (fields.containsKey("amount")) vars.put("amount", fields.get("amount"));

        notificationCommandService.processEvent(new NotificationEvent(
                "ReturnStatusChanged", userId, email, null,
                NotificationChannel.EMAIL, templateId, vars));
    }

    private void handlePaymentCompleted(Map<String, Object> fields) {
        Long orderId = toLong(fields.get("orderId"));
        BigDecimal amount = fields.get("amount") instanceof Number n
                ? BigDecimal.valueOf(n.doubleValue()) : BigDecimal.ZERO;

        // PaymentCompletedEvent doesn't carry userId; skip notification here —
        // order-events SubOrderStatusChanged PAID covers the user notification
        log.debug("Payment completed for order={} amount={} — notification via order-events", orderId, amount);
    }

    private String resolveEmail(Long userId, String guestEmail) {
        if (guestEmail != null && !guestEmail.isBlank()) return guestEmail;
        if (userId == null) return null;
        try {
            Map<String, String> contact = userContactClient.getContact(userId);
            String email = contact.get("email");
            return (email != null && !email.isBlank()) ? email : null;
        } catch (Exception e) {
            log.warn("Could not resolve email for userId={}: {}", userId, e.getMessage());
            return null;
        }
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try { return Long.valueOf(v.toString()); } catch (NumberFormatException e) { return null; }
    }
}
