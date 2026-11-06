-- V2: Fix sub_order status CHECK, add invoices + warranty tables

-- Drop old CHECK on sub_order status (name may differ by Postgres version)
ALTER TABLE sub_order DROP CONSTRAINT IF EXISTS sub_order_status_check;
ALTER TABLE sub_order ADD CONSTRAINT sub_order_status_check CHECK (status IN (
    'PENDING_PAYMENT', 'AWAITING_INVENTORY', 'PAID', 'PROCESSING', 'SHIPPED',
    'READY_FOR_PICKUP', 'DELIVERED', 'RETURN_REQUESTED', 'RETURNED',
    'EXPIRED', 'CANCELLING', 'CANCELLED', 'CANCELLED_OUT_OF_STOCK'
));

-- ── Invoices (Hóa đơn GTGT per Circular 78/2021/TT-BTC) ──────────────────

CREATE SEQUENCE IF NOT EXISTS invoice_number_seq START 1;

CREATE TABLE invoices
(
    id              BIGSERIAL      PRIMARY KEY,
    order_id        BIGINT         NOT NULL REFERENCES orders (id),
    invoice_number  VARCHAR(50)    NOT NULL UNIQUE,
    seller_name     VARCHAR(255)   NOT NULL,
    seller_tax_code VARCHAR(20),
    seller_address  TEXT,
    buyer_name      VARCHAR(255),
    buyer_tax_code  VARCHAR(20),
    buyer_address   TEXT,
    buyer_email     VARCHAR(255),
    subtotal        NUMERIC(14, 2) NOT NULL,
    vat_rate        NUMERIC(5, 4)  NOT NULL DEFAULT 0.1000,
    vat_amount      NUMERIC(14, 2) NOT NULL,
    total_amount    NUMERIC(14, 2) NOT NULL,
    issued_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    issued_by       BIGINT,
    status          VARCHAR(20)    NOT NULL DEFAULT 'ISSUED' CHECK (status IN ('ISSUED', 'CANCELLED')),
    cancel_reason   TEXT,
    cancelled_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX uq_invoice_order_issued ON invoices (order_id) WHERE status = 'ISSUED';
CREATE INDEX idx_invoice_number ON invoices (invoice_number);
CREATE INDEX idx_invoice_order  ON invoices (order_id);

-- ── Warranty ──────────────────────────────────────────────────────────────

CREATE TABLE warranty_items
(
    id            BIGSERIAL    PRIMARY KEY,
    order_item_id BIGINT       NOT NULL REFERENCES order_item (id),
    sub_order_id  BIGINT       NOT NULL REFERENCES sub_order (id),
    sku_id        BIGINT       NOT NULL,
    shop_months   INT          NOT NULL DEFAULT 12,
    mfr_months    INT          NOT NULL DEFAULT 0,
    activated_at  TIMESTAMPTZ,
    expires_at    TIMESTAMPTZ,
    serial_number VARCHAR(100),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_warranty_items_sub_order ON warranty_items (sub_order_id);
CREATE INDEX idx_warranty_items_sku       ON warranty_items (sku_id);

CREATE TABLE warranty_claims
(
    id                   BIGSERIAL    PRIMARY KEY,
    warranty_item_id     BIGINT       NOT NULL REFERENCES warranty_items (id),
    customer_name        VARCHAR(255) NOT NULL,
    customer_phone       VARCHAR(20)  NOT NULL,
    description          TEXT         NOT NULL,
    images               JSONB        NOT NULL DEFAULT '[]',
    handled_by_branch_id BIGINT,
    status               VARCHAR(20)  NOT NULL DEFAULT 'SUBMITTED' CHECK (status IN (
                             'SUBMITTED', 'DIAGNOSING', 'REPAIRING', 'READY', 'RETURNED', 'REJECTED')),
    diagnosis_note       TEXT,
    resolution           VARCHAR(20)  CHECK (resolution IN ('REPAIRED', 'REPLACED', 'REJECTED')),
    resolved_at          TIMESTAMPTZ,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_warranty_claims_item   ON warranty_claims (warranty_item_id);
CREATE INDEX idx_warranty_claims_status ON warranty_claims (status);
