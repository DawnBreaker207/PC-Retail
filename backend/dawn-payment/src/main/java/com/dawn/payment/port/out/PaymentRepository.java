package com.dawn.payment.port.out;

import com.dawn.payment.entity.Payment;
import com.dawn.payment.entity.enums.PaymentMethod;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    List<Payment> findByOrderIdAndMethod(Long orderId, PaymentMethod method);
}
