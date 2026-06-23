package com.dawn.order.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.order.dto.CheckoutRequest;
import com.dawn.order.dto.OrderDto;
import com.dawn.order.entity.Order;
import com.dawn.order.entity.OrderItem;
import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.VoucherUsage;
import com.dawn.order.entity.enums.FulfillmentType;
import com.dawn.order.entity.enums.PaymentStatus;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.order.repository.OrderItemRepository;
import com.dawn.order.repository.OrderRepository;
import com.dawn.order.repository.SubOrderRepository;
import com.dawn.order.repository.VoucherUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutService {
    private static final String VOUCHER_KEY = "voucher:remaining";

    private final OrderRepository orderRepository;
    private final SubOrderRepository subOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final CartService cartService;
    private final VoucherService voucherService;
    private final ReserveStockUseCase reserveStockUseCase;
    private final InventoryCachePort inventoryCachePort;
    private final OrderQueryService orderQueryService;
    private final StringRedisTemplate redisTemplate;

    public OrderDto checkout(CheckoutRequest req, Long userId) {
        // Step 1: Idempotency
        return orderRepository.findByIdempotencyKey(req.idempotencyKey())
                .map(existing -> orderQueryService.toOrderDto(existing))
                .orElseGet(() -> doCheckout(req, userId));
    }

    private OrderDto doCheckout(CheckoutRequest req, Long userId) {
        // Step 2: Load + Validate cart
        List<CartItem> cartItems = cartService.getRawItems(userId);
        if (cartItems.isEmpty()) {
            throw new InvalidRequestException("Cart is empty");
        }


        // Step 3: Validate Voucher
        Voucher voucher = null;
        BigDecimal discountAmount = BigDecimal.ZERO;

        // Calculate temporary subtotal
        BigDecimal subtotal = cartItems.stream()
                .map(item -> BigDecimal.ZERO.multiply(BigDecimal.valueOf(item.getQuantity)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (req.voucherCode() != null) {
            VoucherValidationResult validation = voucherService.validate(req.voucherCode(), subtotal);

            if (!validation.valid()) {
                throw new InvalidRequestException(validation.errorMessage());
            }
            throw new InvalidRequestException(validation.errorMessage());
            discountAmount = validation.discountAmount();
        }


        // Step 4: Lua gatekeeper
        List<StockDecrementCommand> stockItems = cartItems.stream()
                .map(item -> new StockDecrementCommand(item.getSkuId(), item.getQuantity()))
                .toList();

        List<Long> failedSkuIds = inventoryCachePort.decrementBatch(stockItems);
        if (!failedSkuIds.isEmpty()) {
            throw new InvalidRequestException("This product is not enough item: " + failedSkuIds);
        }


        // Step 5: DB Transaction
        try {
            return executeTransaction(req, userId, cartItems, stockItems, voucher, discountAmount, subtotal);
        } catch (Exception e) {
            log.error("Checkout DB transaction failed, compensating Redis", e);
            inventoryCachePort.incrementBatch(stockItems);
            throw e;
        }
    }

    @Transactional
    protected OrderDto executeTransaction(
            CheckoutRequest req,
            Long userId,
            List<CartItem> cartItems,
            List<StockDecrementCommand> stockItems,
            Voucher voucher,
            BigDecimal discountAmount,
            BigDecimal subtotal) {
        BigDecimal shippingFee = req.fulfillmentType() == FulfillmentType.DELIVERY ? BigDecimal.valueOf(30000) : BigDecimal.ZERO;
        BigDecimal finalAmount = subtotal.subtract(discountAmount).add(shippingFee);

        // Create Order
        Order order = Order.builder()
                .userId(userId)
                .shippingAddress(Map.of())
                .totalAmount(finalAmount)
                .taxAmount(BigDecimal.ZERO)
                .paymentStatus(PaymentStatus.PENDING)
                .reservedUntil(Instant.now().plusSeconds(15 * 60))
                .idempotencyKey(req.idempotencyKey())
                .taxInfo(req.taxInfo())
                .build();
        orderRepository.save(order);

        // Create SubOrder
        SubOrder subOrder = SubOrder.builder()
                .orderId(order.getId())
                .fulfillmentType(req.fulfillmentType())
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .status(SubOrderStatus.PENDING_PAYMENT)
                .statusHistory(new ArrayList<>(List.of(Map.of(
                        "from", "CREATED",
                        "to", SubOrderStatus.PENDING_PAYMENT.name(),
                        "createdAt", Instant.now().toString()
                ))))
                .build();
        subOrderRepository.save(subOrder);

        // Create OrderItems
        List<OrderItem> orderItems = cartItems.stream()
                .map(item -> OrderItem.builder()
                        .subOrderId(subOrder.getId())
                        .skuId(item.getSkuId())
                        .quantity(item.getQuantity())
                        .unitPrice(BigDecimal.ZERO)
                        .build())
                .toList();
        orderItemRepository.save(orderItems);

        // Reserve inventory with pessimistic lock
        reserveStockUseCase.reserve(stockItems, order.getId());

        // Voucher usage
        if (voucher != null) {
            // Decrement Redis
            redisTemplate.opsForValue().decrement(VOUCHER_KEY + voucher.getId());

            // Write VoucherUsage
            voucherUsageRepository.save(VoucherUsage.builder()
                    .voucherId(voucher.getId())
                    .userId(userId)
                    .orderId(order.getId())
                    .discountAmount(discountAmount)
                    .build());
        }

        // Step 7: AFTER COMMIT - Clear Cart
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cartService.clearCart(userId);
            }
        });

        return orderQueryService.toOrderDto(order);
    }
}
