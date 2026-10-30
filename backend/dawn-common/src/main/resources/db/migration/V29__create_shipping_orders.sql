-- V29: Shipping orders table for dawn-shipping service
CREATE TABLE IF NOT EXISTS shipping_orders (
    id               BIGSERIAL PRIMARY KEY,
    order_id         BIGINT       NOT NULL,
    user_id          BIGINT,
    tracking_code    VARCHAR(100),
    carrier          VARCHAR(50)  NOT NULL DEFAULT 'MOCK',
    status           VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    from_address     TEXT,
    to_address       TEXT         NOT NULL,
    weight_grams     INTEGER,
    shipping_fee     NUMERIC(15, 2),
    estimated_delivery TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_shipping_order_id ON shipping_orders (order_id);
CREATE INDEX IF NOT EXISTS idx_shipping_user_created ON shipping_orders (user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_shipping_status ON shipping_orders (status);
