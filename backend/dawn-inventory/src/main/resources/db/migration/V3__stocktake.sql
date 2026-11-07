CREATE TABLE IF NOT EXISTS stocktake (
    id              BIGSERIAL PRIMARY KEY,
    warehouse_id    BIGINT,
    status          VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    created_by      BIGINT,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS stocktake_item (
    id                BIGSERIAL PRIMARY KEY,
    stocktake_id      BIGINT       NOT NULL REFERENCES stocktake(id),
    sku_id            BIGINT       NOT NULL,
    system_quantity   INT          NOT NULL DEFAULT 0,
    counted_quantity  INT          NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_stocktake_item_session ON stocktake_item(stocktake_id);
