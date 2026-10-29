CREATE TABLE inventory
(
    id                BIGSERIAL PRIMARY KEY,
    sku_id            BIGINT      NOT NULL UNIQUE REFERENCES sku (id),
    stock_quantity    INTEGER     NOT NULL DEFAULT 0,
    reserved_quantity INTEGER     NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE inventory_transaction
(
    id             BIGSERIAL PRIMARY KEY,
    sku_id         BIGINT      NOT NULL REFERENCES sku (id),
    type           VARCHAR(20) NOT NULL CHECK (type IN ('RESERVE', 'RELEASE', 'DEDUCT', 'RESTOCK', 'ADJUST')),
    quantity       INTEGER     NOT NULL,
    unit_cost      NUMERIC(14, 2),
    reference_type VARCHAR(30),
    reference_id   BIGINT,
    created_by     BIGINT REFERENCES users (id),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_inventory_txn_sku ON inventory_transaction (sku_id);
CREATE INDEX idx_inventory_txn_reference ON inventory_transaction (reference_type, reference_id);