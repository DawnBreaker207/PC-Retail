-- V31: Tax invoice table for VAT calculation and invoice generation
CREATE TABLE IF NOT EXISTS tax_invoices
(
    id                BIGSERIAL PRIMARY KEY,
    order_id          BIGINT        NOT NULL UNIQUE,
    user_id           BIGINT,
    buyer_type        VARCHAR(10)   NOT NULL DEFAULT 'B2C',
    subtotal          NUMERIC(15,2) NOT NULL,
    tax_amount        NUMERIC(15,2) NOT NULL,
    total             NUMERIC(15,2) NOT NULL,
    tax_rate          NUMERIC(5,4)  NOT NULL,
    invoice_number    VARCHAR(50) UNIQUE,
    invoice_status    VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    business_tax_code VARCHAR(20),
    issued_at         TIMESTAMPTZ,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_tax_invoice_user ON tax_invoices (user_id, created_at DESC);
CREATE SEQUENCE IF NOT EXISTS tax_invoice_seq START 1000;
