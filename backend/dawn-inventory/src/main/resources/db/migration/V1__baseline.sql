-- dawn-inventory: inventory stock, transactions, stock receipts

CREATE TABLE inventory
(
    id                BIGSERIAL   PRIMARY KEY,
    sku_id            BIGINT      NOT NULL UNIQUE,
    stock_quantity    INTEGER     NOT NULL DEFAULT 0,
    reserved_quantity INTEGER     NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_inventory_stock_nonneg    CHECK (stock_quantity >= 0),
    CONSTRAINT chk_inventory_reserved_nonneg CHECK (reserved_quantity >= 0),
    CONSTRAINT chk_inventory_reserved_lte_stock CHECK (reserved_quantity <= stock_quantity)
);

CREATE TABLE inventory_transaction
(
    id             BIGSERIAL      PRIMARY KEY,
    sku_id         BIGINT         NOT NULL,
    type           VARCHAR(20)    NOT NULL CHECK (type IN ('RESERVE', 'RELEASE', 'DEDUCT', 'RESTOCK', 'ADJUST')),
    quantity       INTEGER        NOT NULL,
    unit_cost      NUMERIC(14, 2),
    reason         TEXT,
    reference_type VARCHAR(30),
    reference_id   BIGINT,
    created_by     BIGINT,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_inventory_txn_sku       ON inventory_transaction (sku_id);
CREATE INDEX idx_inventory_txn_reference ON inventory_transaction (reference_type, reference_id);

CREATE TABLE stock_receipt
(
    id           BIGSERIAL   PRIMARY KEY,
    status       VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by   BIGINT,
    confirmed_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE stock_receipt_item
(
    id         BIGSERIAL      PRIMARY KEY,
    receipt_id BIGINT         NOT NULL REFERENCES stock_receipt (id) ON DELETE CASCADE,
    sku_id     BIGINT         NOT NULL,
    quantity   INTEGER        NOT NULL CHECK (quantity > 0),
    unit_cost  NUMERIC(14, 2),
    created_at TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ    NOT NULL DEFAULT now()
);
