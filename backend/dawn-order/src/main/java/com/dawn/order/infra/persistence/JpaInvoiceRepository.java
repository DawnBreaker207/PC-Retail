package com.dawn.order.infra.persistence;

import com.dawn.order.entity.Invoice;
import com.dawn.order.entity.enums.InvoiceStatus;
import com.dawn.order.port.out.InvoiceRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaInvoiceRepository extends JpaRepository<Invoice, Long>, InvoiceRepository {

    @Override
    Optional<Invoice> findByOrderIdAndStatus(Long orderId, InvoiceStatus status);

    @Override
    @Query(value = "SELECT nextval('invoice_number_seq')", nativeQuery = true)
    long nextInvoiceSequence();
}
