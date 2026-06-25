package com.dawn.order.service;

import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.common.response.ResponsePage;
import com.dawn.order.dto.OrderDto;
import com.dawn.order.dto.SubOrderDto;
import com.dawn.order.entity.Order;
import com.dawn.order.port.in.QueryOrderUseCase;
import com.dawn.order.port.out.OrderRepositoryPort;
import com.dawn.order.port.out.SubOrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderQueryService implements QueryOrderUseCase {
    private final OrderRepositoryPort orderRepo;
    private final SubOrderRepositoryPort subOrderRepo;

    @Override
    public ResponsePage<OrderDto> getMyOrders(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponsePage.of(orderRepo.findByUserId(userId, pageable).map(this::toOrderDto));
    }

    @Override
    public OrderDto getById(Long orderId, Long userId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.Order.NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException(Message.Order.NOT_FOUND);
        }
        return toOrderDto(order);
    }

    @Override
    public OrderDto toOrderDto(Order order) {
        List<SubOrderDto> subOrders = subOrderRepo.findByOrderId(order.getId()).stream()
                .map(s -> new SubOrderDto(
                        s.getId(),
                        s.getFulfillmentType(),
                        s.getSubtotal(),
                        s.getShippingFee(),
                        s.getDiscountAmount(),
                        s.getFinalAmount(),
                        s.getStatus(),
                        List.of()))
                .toList();

        return new OrderDto(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getPaymentStatus(),
                order.getReservedUntil(),
                subOrders);
    }
}
