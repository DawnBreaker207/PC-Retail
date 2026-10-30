package com.dawn.tax.repository;

import com.dawn.tax.entity.TaxInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaxInvoiceRepository extends JpaRepository<TaxInvoice, Long> {
    Optional<TaxInvoice> findByOrderId(Long orderId);
    Page<TaxInvoice> findByUserId(Long userId, Pageable pageable);
}
