CREATE TABLE orders
(

    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT         NOT NULL REFERENCES users (id),
    shipping_address     JSONB          NOT NULL,
    total_amount         NUMERIC(14, 2) NOT NULL,
    tax_amount           NUMERIC(14, 2) NOT NULL DEFAULT 0,
    payment_status       VARCHAR(20)    NOT NULL DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED', 'EXPIRED')),
    reserved_until       TIMESTAMPTZ,
    idempotency_key      VARCHAR(100)   NOT NULL UNIQUE,
    created_by_type      VARCHAR(20)    NOT NULL DEFAULT 'SELF',
    assisted_by_staff_id BIGINT REFERENCES users (id),
    invoice_requested    BOOLEAN        NOT NULL DEFAULT false,
    tax_info             JSONB,
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_order_user ON orders (user_id);
CREATE INDEX idx_order_payment_status ON orders (payment_status);

CREATE TABLE sub_order
(
    id               BIGSERIAL PRIMARY KEY,
    order_id         BIGINT         NOT NULL REFERENCES orders (id),
    fulfillment_type VARCHAR(10)    NOT NULL CHECK (fulfillment_type IN ('DELIVERY', 'PICKUP')),
    subtotal         NUMERIC(14, 2) NOT NULL,
    shipping_fee     NUMERIC(14, 2) NOT NULL DEFAULT 0,
    discount_amount  NUMERIC(14, 2) NOT NULL DEFAULT 0,
    final_amount     NUMERIC(14, 2) NOT NULL,
    status           VARCHAR(30)    NOT NULL DEFAULT 'PENDING_PAYMENT' CHECK (status IN (
                                                                                         'PENDING_PAYMENT', 'PAID',
                                                                                         'PROCESSING', 'SHIPPED',
                                                                                         'READY_FOR_PICKUP',
                                                                                         'DELIVERED',
                                                                                         'RETURN_REQUESTED',
                                                                                         'RETURNED', 'EXPIRED',
                                                                                         'CANCELLED'
        )),
    tracking_code    VARCHAR(100),
    carrier          JSONB,
    webhook_log      JSONB,
    status_history   JSONB          NOT NULL DEFAULT '[]',
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_sub_order_order ON sub_order (order_id);
CREATE INDEX idx_sub_order_status ON sub_order (status);


CREATE TABLE order_item
(
    id                 BIGSERIAL PRIMARY KEY,
    sub_order_id       BIGINT         NOT NULL REFERENCES sub_order (id),
    sku_id             BIGINT         NOT NULL REFERENCES sku (id),
    quantity           INTEGER        NOT NULL CHECK ( quantity > 0 ),
    unit_price         NUMERIC(14, 2) NOT NULL,
    discount_allocated NUMERIC(14, 2) NOT NULL DEFAULT 0
);
CREATE INDEX idx_order_item_sub_order ON order_item (sub_order_id);
CREATE INDEX idx_order_item_sku ON order_item (sku_id);

CREATE TABLE voucher_usage
(
    id              BIGSERIAL PRIMARY KEY,
    voucher_id      BIGINT         NOT NULL REFERENCES voucher (id),
    user_id         BIGINT         NOT NULL REFERENCES users (id),
    order_id        BIGINT         NOT NULL REFERENCES orders (id),
    discount_amount NUMERIC(14, 2) NOT NULL,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    UNIQUE (voucher_id, user_id)
);

CREATE INDEX idx_voucher_usage_order ON voucher_usage (order_id);