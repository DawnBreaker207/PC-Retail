-- Mock payment gateway: cho phép method MOCK (dev/demo/black-box thay MoMo/VNPay thật)
ALTER TABLE payment DROP CONSTRAINT IF EXISTS payment_method_check;
ALTER TABLE payment ADD CONSTRAINT payment_method_check
    CHECK (method IN ('CASH', 'CARD', 'MOMO', 'VNPAY', 'INSTALLMENT', 'MOCK'));
