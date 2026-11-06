package com.dawn.order.port.out;

import com.dawn.order.entity.Invoice;
import com.dawn.order.entity.enums.InvoiceStatus;

import java.util.Optional;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(Long id);
    Optional<Invoice> findByOrderIdAndStatus(Long orderId, InvoiceStatus status);
    long nextInvoiceSequence();
}
