-- dawn-order: promotion, cart, orders, rma, engagement, reporting, shipping

-- ── Promotion ─────────────────────────────────────────────────────────────

CREATE TABLE voucher
(
    id                  BIGSERIAL      PRIMARY KEY,
    code                VARCHAR(50)    NOT NULL UNIQUE,
    scope_type          VARCHAR(20)    NOT NULL CHECK (scope_type IN ('STOREWIDE', 'CATEGORY', 'PRODUCT', 'FREESHIP')),
    scope_id            BIGINT,
    discount_type       VARCHAR(10)    NOT NULL CHECK (discount_type IN ('FIXED', 'PERCENT')),
    discount_value      NUMERIC(14, 2) NOT NULL,
    max_discount_amount NUMERIC(14, 2),
    min_order_value     NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total_quantity      INTEGER        NOT NULL,
    remaining_quantity  INTEGER        NOT NULL,
    valid_from          TIMESTAMPTZ    NOT NULL,
    valid_to            TIMESTAMPTZ    NOT NULL,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
CREATE INDEX idx_voucher_code  ON voucher (code) WHERE deleted_at IS NULL;
CREATE INDEX idx_voucher_valid ON voucher (valid_from, valid_to);

CREATE TABLE flash_sale_campaign
(
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    start_time TIMESTAMPTZ  NOT NULL,
    end_time   TIMESTAMPTZ  NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'UPCOMING' CHECK (status IN ('UPCOMING', 'ACTIVE', 'ENDED')),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_flash_sale_campaign_status ON flash_sale_campaign (status);

CREATE TABLE flash_sale_item
(
    id             BIGSERIAL      PRIMARY KEY,
    campaign_id    BIGINT         NOT NULL REFERENCES flash_sale_campaign (id),
    sku_id         BIGINT         NOT NULL,
    flash_price    NUMERIC(14, 2) NOT NULL,
    quantity_limit INTEGER        NOT NULL,
    UNIQUE (campaign_id, sku_id)
);
CREATE INDEX idx_flash_sale_item_sku ON flash_sale_item (sku_id);

-- ── Cart ──────────────────────────────────────────────────────────────────

CREATE TABLE cart_item
(
    id          BIGSERIAL   PRIMARY KEY,
    user_id     BIGINT,
    sku_id      BIGINT      NOT NULL,
    quantity    INTEGER     NOT NULL CHECK (quantity > 0),
    guest_token VARCHAR(100),
    added_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version     BIGINT      NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, sku_id),
    CONSTRAINT chk_cart_owner CHECK (
        (user_id IS NOT NULL AND guest_token IS NULL)
        OR (user_id IS NULL AND guest_token IS NOT NULL)
    )
);
CREATE UNIQUE INDEX uq_cart_guest_sku ON cart_item (guest_token, sku_id) WHERE guest_token IS NOT NULL;

-- ── Orders ────────────────────────────────────────────────────────────────

CREATE TABLE orders
(
    id                   BIGSERIAL      PRIMARY KEY,
    user_id              BIGINT,
    guest_email          VARCHAR(255),
    guest_phone          VARCHAR(20),
    shipping_address     JSONB          NOT NULL,
    total_amount         NUMERIC(14, 2) NOT NULL,
    tax_amount           NUMERIC(14, 2) NOT NULL DEFAULT 0,
    payment_status       VARCHAR(20)    NOT NULL DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED', 'EXPIRED')),
    reserved_until       TIMESTAMPTZ,
    idempotency_key      VARCHAR(100)   NOT NULL,
    created_by_type      VARCHAR(20)    NOT NULL DEFAULT 'SELF',
    assisted_by_staff_id BIGINT,
    invoice_requested    BOOLEAN        NOT NULL DEFAULT false,
    tax_info             JSONB,
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_orders_user_idempotency UNIQUE (user_id, idempotency_key)
);
CREATE INDEX idx_order_user           ON orders (user_id);
CREATE INDEX idx_order_payment_status ON orders (payment_status);

CREATE TABLE sub_order
(
    id              BIGSERIAL      PRIMARY KEY,
    order_id        BIGINT         NOT NULL REFERENCES orders (id),
    fulfillment_type VARCHAR(10)   NOT NULL CHECK (fulfillment_type IN ('DELIVERY', 'PICKUP')),
    subtotal        NUMERIC(14, 2) NOT NULL,
    shipping_fee    NUMERIC(14, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,
    final_amount    NUMERIC(14, 2) NOT NULL,
    status          VARCHAR(30)    NOT NULL DEFAULT 'PENDING_PAYMENT' CHECK (status IN (
                        'PENDING_PAYMENT', 'PAID', 'PROCESSING', 'SHIPPED',
                        'READY_FOR_PICKUP', 'DELIVERED', 'RETURN_REQUESTED',
                        'RETURNED', 'EXPIRED', 'CANCELLED')),
    tracking_code   VARCHAR(100),
    carrier         JSONB,
    webhook_log     JSONB,
    status_history  JSONB          NOT NULL DEFAULT '[]',
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_sub_order_order  ON sub_order (order_id);
CREATE INDEX idx_sub_order_status ON sub_order (status);

CREATE TABLE order_item
(
    id                 BIGSERIAL      PRIMARY KEY,
    sub_order_id       BIGINT         NOT NULL REFERENCES sub_order (id),
    sku_id             BIGINT         NOT NULL,
    product_name       VARCHAR(500),
    sku_code           VARCHAR(100),
    quantity           INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price         NUMERIC(14, 2) NOT NULL,
    discount_allocated NUMERIC(14, 2) NOT NULL DEFAULT 0
);
CREATE INDEX idx_order_item_sub_order ON order_item (sub_order_id);
CREATE INDEX idx_order_item_sku       ON order_item (sku_id);

CREATE TABLE voucher_usage
(
    id              BIGSERIAL      PRIMARY KEY,
    voucher_id      BIGINT         NOT NULL REFERENCES voucher (id),
    user_id         BIGINT         NOT NULL,
    order_id        BIGINT         NOT NULL REFERENCES orders (id),
    discount_amount NUMERIC(14, 2) NOT NULL,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    UNIQUE (voucher_id, user_id)
);
CREATE INDEX idx_voucher_usage_order ON voucher_usage (order_id);

-- ── RMA ───────────────────────────────────────────────────────────────────

CREATE TABLE return_request
(
    id                  BIGSERIAL   PRIMARY KEY,
    sub_order_id        BIGINT      NOT NULL REFERENCES sub_order (id),
    requested_by        BIGINT      NOT NULL,
    reason              TEXT        NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'REQUESTED' CHECK (status IN (
                            'REQUESTED', 'APPROVED', 'REJECTED', 'REFUNDED', 'DISPUTED')),
    images              JSONB       NOT NULL DEFAULT '[]',
    reviewed_by         BIGINT,
    reviewed_at         TIMESTAMPTZ,
    disputed_at         TIMESTAMPTZ,
    dispute_resolved_by BIGINT,
    dispute_resolution  VARCHAR(20) CHECK (dispute_resolution IN ('UPHOLD_REJECT', 'OVERTURN_APPROVE')),
    dispute_note        TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_return_request_sub_order ON return_request (sub_order_id);
CREATE INDEX idx_return_request_status    ON return_request (status);

CREATE TABLE refund
(
    id                BIGSERIAL      PRIMARY KEY,
    return_request_id BIGINT         REFERENCES return_request (id),
    sub_order_id      BIGINT         NOT NULL REFERENCES sub_order (id),
    amount            NUMERIC(14, 2) NOT NULL,
    status            VARCHAR(20)    NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    refund_method     VARCHAR(20)    NOT NULL,
    processed_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT now()
);
CREATE INDEX idx_refund_sub_order ON refund (sub_order_id);

-- ── Engagement ────────────────────────────────────────────────────────────

CREATE TABLE review
(
    id           BIGSERIAL   PRIMARY KEY,
    sub_order_id BIGINT      NOT NULL REFERENCES sub_order (id),
    sku_id       BIGINT      NOT NULL,
    user_id      BIGINT      NOT NULL,
    rating       SMALLINT    NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment      TEXT,
    shop_reply   TEXT,
    shop_reply_at TIMESTAMPTZ,
    images       JSONB,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_review_suborder_sku UNIQUE (sub_order_id, sku_id)
);

CREATE TABLE notification
(
    id             BIGSERIAL    PRIMARY KEY,
    user_id        BIGINT       NOT NULL,
    type           VARCHAR(50)  NOT NULL,
    title          VARCHAR(255) NOT NULL,
    body           TEXT,
    reference_id   BIGINT,
    reference_type VARCHAR(50),
    is_read        BOOLEAN      NOT NULL DEFAULT false,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_notification_user ON notification (user_id);

CREATE TABLE article
(
    id            BIGSERIAL    PRIMARY KEY,
    title         VARCHAR(500) NOT NULL,
    slug          VARCHAR(500) NOT NULL UNIQUE,
    content       TEXT,
    thumbnail_url VARCHAR(500),
    category      VARCHAR(30)  NOT NULL DEFAULT 'NEWS',
    status        VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'HIDDEN')),
    author_id     BIGINT,
    published_at  TIMESTAMPTZ,
    template_id   BIGINT,
    deleted_at    TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Shipping ──────────────────────────────────────────────────────────────

CREATE TABLE shipping_zone
(
    id         BIGSERIAL      PRIMARY KEY,
    zone_code  VARCHAR(50)    NOT NULL UNIQUE,
    match_city VARCHAR(100),
    fee        NUMERIC(14, 2) NOT NULL,
    is_active  BOOLEAN        NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ    NOT NULL DEFAULT now()
);
INSERT INTO shipping_zone (zone_code, match_city, fee, is_active)
VALUES ('DEFAULT', NULL, 30000, true)
ON CONFLICT (zone_code) DO NOTHING;

CREATE TABLE shipping_orders
(
    id                 BIGSERIAL      PRIMARY KEY,
    order_id           BIGINT         NOT NULL,
    user_id            BIGINT,
    tracking_code      VARCHAR(100),
    carrier            VARCHAR(50)    NOT NULL DEFAULT 'MOCK',
    status             VARCHAR(30)    NOT NULL DEFAULT 'PENDING',
    from_address       TEXT,
    to_address         TEXT           NOT NULL,
    weight_grams       INTEGER,
    shipping_fee       NUMERIC(15, 2),
    estimated_delivery TIMESTAMPTZ,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ    NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX uidx_shipping_order_id   ON shipping_orders (order_id);
CREATE INDEX        idx_shipping_user_created ON shipping_orders (user_id, created_at DESC);
CREATE INDEX        idx_shipping_status       ON shipping_orders (status);

-- ── Reporting ─────────────────────────────────────────────────────────────

CREATE TABLE supplier
(
    id           BIGSERIAL    PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    contact_info JSONB,
    tax_code     VARCHAR(50),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at   TIMESTAMPTZ
);

CREATE TABLE purchase_order
(
    id          BIGSERIAL      PRIMARY KEY,
    supplier_id BIGINT         NOT NULL REFERENCES supplier (id),
    status      VARCHAR(20)    NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'CONFIRMED', 'RECEIVED')),
    total_cost  NUMERIC(14, 2) NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE TABLE purchase_order_item
(
    id        BIGSERIAL      PRIMARY KEY,
    po_id     BIGINT         NOT NULL REFERENCES purchase_order (id),
    sku_id    BIGINT         NOT NULL,
    quantity  INTEGER        NOT NULL CHECK (quantity > 0),
    unit_cost NUMERIC(14, 2) NOT NULL
);

CREATE TABLE stock_take_session
(
    id           BIGSERIAL   PRIMARY KEY,
    status       VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
    started_by   BIGINT      NOT NULL,
    started_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE stock_take_item
(
    id              BIGSERIAL   PRIMARY KEY,
    session_id      BIGINT      NOT NULL REFERENCES stock_take_session (id),
    sku_id          BIGINT      NOT NULL,
    system_quantity INTEGER     NOT NULL,
    counted_quantity INTEGER    NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
