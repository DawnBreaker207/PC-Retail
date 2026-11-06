package com.dawn.order.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.order.config.InvoiceConfig;
import com.dawn.order.dto.InvoiceDto;
import com.dawn.order.entity.Invoice;
import com.dawn.order.entity.Order;
import com.dawn.order.entity.enums.InvoiceStatus;
import com.dawn.order.event.SubOrderDeliveredEvent;
import com.dawn.order.port.out.InvoiceRepository;
import com.dawn.order.port.out.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    private final InvoiceRepository invoiceRepo;
    private final OrderRepository orderRepo;
    private final InvoiceConfig invoiceConfig;

    @Async
    @EventListener
    public void onDelivered(SubOrderDeliveredEvent event) {
        orderRepo.findById(event.orderId()).ifPresent(order -> {
            if (order.isInvoiceRequested()) {
                try {
                    generate(event.orderId(), order, null);
                } catch (Exception e) {
                    log.error("Auto-invoice generation failed for order={}", event.orderId(), e);
                }
            }
        });
    }

    @Transactional
    public InvoiceDto requestInvoice(Long orderId, Long userId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Order.NOT_FOUND, orderId)));

        if (order.getUserId() != null && !order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException(Message.format(Message.Order.NOT_FOUND, orderId));
        }

        boolean alreadyIssued = invoiceRepo.findByOrderIdAndStatus(orderId, InvoiceStatus.ISSUED).isPresent();
        if (alreadyIssued) {
            throw new ConflictException(Message.Order.INVOICE_ALREADY_ISSUED);
        }

        return toDto(generate(orderId, order, userId));
    }

    @Transactional
    public InvoiceDto getByOrderId(Long orderId) {
        Invoice invoice = invoiceRepo.findByOrderIdAndStatus(orderId, InvoiceStatus.ISSUED)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Message.format(Message.Order.INVOICE_NOT_FOUND, orderId)));
        return toDto(invoice);
    }

    private Invoice generate(Long orderId, Order order, Long issuedBy) {
        BigDecimal subtotal = order.getTotalAmount();
        BigDecimal vatRate = BigDecimal.valueOf(invoiceConfig.getVatRate());
        BigDecimal vatAmount = subtotal.multiply(vatRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(vatAmount);

        Map<String, Object> taxInfo = order.getTaxInfo();
        String buyerName = extract(taxInfo, "buyerName");
        String buyerTaxCode = extract(taxInfo, "buyerTaxCode");
        String buyerAddress = extract(taxInfo, "buyerAddress");
        String buyerEmail = extract(taxInfo, "buyerEmail");

        String invoiceNumber = buildInvoiceNumber();

        Invoice invoice = Invoice.builder()
                .orderId(orderId)
                .invoiceNumber(invoiceNumber)
                .sellerName(invoiceConfig.getSellerName())
                .sellerTaxCode(invoiceConfig.getSellerTaxCode())
                .sellerAddress(invoiceConfig.getSellerAddress())
                .buyerName(buyerName)
                .buyerTaxCode(buyerTaxCode)
                .buyerAddress(buyerAddress)
                .buyerEmail(buyerEmail)
                .subtotal(subtotal)
                .vatRate(vatRate)
                .vatAmount(vatAmount)
                .totalAmount(total)
                .issuedAt(Instant.now())
                .issuedBy(issuedBy)
                .status(InvoiceStatus.ISSUED)
                .build();

        return invoiceRepo.save(invoice);
    }

    private String buildInvoiceNumber() {
        long seq = invoiceRepo.nextInvoiceSequence();
        String date = DATE_FMT.format(Instant.now());
        return String.format("HD-%s-%06d", date, seq);
    }

    private String extract(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    private InvoiceDto toDto(Invoice i) {
        return new InvoiceDto(i.getId(), i.getOrderId(), i.getInvoiceNumber(),
                i.getSellerName(), i.getSellerTaxCode(), i.getSellerAddress(),
                i.getBuyerName(), i.getBuyerTaxCode(), i.getBuyerAddress(), i.getBuyerEmail(),
                i.getSubtotal(), i.getVatRate(), i.getVatAmount(), i.getTotalAmount(),
                i.getIssuedAt(), i.getStatus());
    }
}
