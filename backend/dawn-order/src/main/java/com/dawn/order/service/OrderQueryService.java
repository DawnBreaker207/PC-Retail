package com.dawn.order.service;

import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.common.response.ResponsePage;
import com.dawn.order.dto.OrderDto;
import com.dawn.order.dto.OrderItemDto;
import com.dawn.order.dto.SubOrderDto;
import com.dawn.order.entity.Order;
import com.dawn.order.entity.OrderItem;
import com.dawn.order.entity.SubOrder;
import com.dawn.order.port.in.QueryOrderUseCase;
import com.dawn.common.contract.CatalogClientPort;
import com.dawn.order.port.out.OrderItemRepository;
import com.dawn.order.port.out.OrderRepository;
import com.dawn.order.port.out.SubOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderQueryService implements QueryOrderUseCase {
    private final OrderRepository orderRepo;
    private final SubOrderRepository subOrderRepo;
    private final OrderItemRepository orderItemRepo;
    private final CatalogClientPort catalogClient;

    @Override
    @Transactional(readOnly = true)
    public ResponsePage<OrderDto> getMyOrders(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponsePage.of(orderRepo.findByUserId(userId, pageable).map(this::toOrderDto));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponsePage<OrderDto> getAllForStaff(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponsePage.of(orderRepo.findAll(pageable).map(this::toOrderDto));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getByIdForStaff(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Order.NOT_FOUND, orderId)));
        return toOrderDto(order);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<OrderDto> findByIdempotencyKey(String key) {
        return orderRepo.findByIdempotencyKey(key)
                .map(this::toOrderDto);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<OrderDto> findByUserIdAndIdempotencyKey(Long userId, String key) {
        return orderRepo.findByUserIdAndIdempotencyKey(userId, key)
                .map(this::toOrderDto);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getById(Long orderId, Long userId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Order.NOT_FOUND, orderId)));

        if (!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException(Message.format(Message.Order.NOT_FOUND, orderId));
        }
        return toOrderDto(order);
    }

    @Override
    public OrderDto toOrderDto(Order order) {
        List<SubOrder> subOrders = subOrderRepo.findByOrderId(order.getId());

        List<Long> allSkuIds = subOrders
                .stream()
                .flatMap(s -> orderItemRepo.findBySubOrderId(s.getId()).stream())
                .map(OrderItem::getSkuId)
                .distinct()
                .toList();


        Map<Long, String> nameMap = allSkuIds.isEmpty()
                ? Map.of()
                : catalogClient.getNames(allSkuIds);

        List<SubOrderDto> subOrderDtos = subOrders.stream()
                .map(s -> {
                    List<OrderItemDto> items = orderItemRepo.findBySubOrderId(s.getId()).stream()
                            .map(i -> new OrderItemDto(
                                    i.getSkuId(),
                                    nameMap.getOrDefault(i.getSkuId(), null),
                                    i.getQuantity(),
                                    i.getUnitPrice(),
                                    i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))))
                            .toList();
                    return new SubOrderDto(
                            s.getId(),
                            s.getFulfillmentType(),
                            s.getSubtotal(),
                            s.getShippingFee(),
                            s.getDiscountAmount(),
                            s.getFinalAmount(),
                            s.getStatus(),
                            items);
                })
                .toList();

        return new OrderDto(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getPaymentStatus(),
                order.getReservedUntil(),
                subOrderDtos);
    }


}
