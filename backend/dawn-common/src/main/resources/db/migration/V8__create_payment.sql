CREATE TABLE payment
(
    id                  BIGSERIAL PRIMARY KEY,
    order_id            BIGINT         NOT NULL REFERENCES orders (id),
    method              VARCHAR(20)    NOT NULL CHECK (method IN ('CASH', 'CARD', 'MOMO', 'VNPAY', 'INSTALLMENT')),
    installment_months  INTEGER,
    amount              NUMERIC(14, 2) NOT NULL,
    status              VARCHAR(20)    NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'UNKNOWN')),
    transaction_ref     VARCHAR(100),
    settled_at          TIMESTAMPTZ,
    settlement_batch_id VARCHAR(100),
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);