package com.dawn.payment.infra.persistence;

import com.dawn.payment.entity.Payment;
import com.dawn.payment.entity.enums.PaymentMethod;
import com.dawn.payment.port.out.PaymentRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaPaymentRepository extends JpaRepository<Payment, Long>, PaymentRepositoryPort {
    @Override
    List<Payment> findByOrderIdAndMethod(Long orderId, PaymentMethod method);
}
