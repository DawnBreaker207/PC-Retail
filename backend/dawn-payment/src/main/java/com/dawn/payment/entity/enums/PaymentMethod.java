package com.dawn.payment.entity.enums;

public enum PaymentMethod {
    CASH,
    CARD,
    MOMO,
    VNPAY,
    INSTALLMENT,
    // Mock gateway cho dev/demo/black-box (thay MoMo/VNPay thật)
    MOCK
}
