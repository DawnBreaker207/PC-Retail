-- dawn-payment: payments and tax invoices

CREATE TABLE payment
(
    id                  BIGSERIAL      PRIMARY KEY,
    order_id            BIGINT         NOT NULL,
    user_id             BIGINT,
    method              VARCHAR(20)    NOT NULL CHECK (method IN ('CASH', 'CARD', 'MOMO', 'VNPAY', 'INSTALLMENT', 'MOCK')),
    installment_months  INTEGER,
    amount              NUMERIC(14, 2) NOT NULL,
    status              VARCHAR(20)    NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'UNKNOWN')),
    transaction_ref     VARCHAR(100),
    settled_at          TIMESTAMPTZ,
    settlement_batch_id VARCHAR(100),
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);
CREATE INDEX        idx_payment_order_id          ON payment (order_id);
CREATE UNIQUE INDEX uidx_payment_transaction_ref  ON payment (transaction_ref) WHERE transaction_ref IS NOT NULL;

CREATE TABLE tax_invoices
(
    id                BIGSERIAL      PRIMARY KEY,
    order_id          BIGINT         NOT NULL UNIQUE,
    user_id           BIGINT,
    buyer_type        VARCHAR(10)    NOT NULL DEFAULT 'B2C',
    subtotal          NUMERIC(15, 2) NOT NULL,
    tax_amount        NUMERIC(15, 2) NOT NULL,
    total             NUMERIC(15, 2) NOT NULL,
    tax_rate          NUMERIC(5, 4)  NOT NULL,
    invoice_number    VARCHAR(50) UNIQUE,
    invoice_status    VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    business_tax_code VARCHAR(20),
    issued_at         TIMESTAMPTZ,
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT now()
);
CREATE INDEX idx_tax_invoice_user ON tax_invoices (user_id, created_at DESC);
CREATE SEQUENCE IF NOT EXISTS tax_invoice_seq START 1000;
