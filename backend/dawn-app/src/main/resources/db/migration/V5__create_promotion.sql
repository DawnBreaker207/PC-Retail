CREATE TABLE voucher
(
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(50)    NOT NULL UNIQUE,
    scope_type          VARCHAR(20)    NOT NULL CHECK ((scope_type IN ('STOREWIDE', 'CATEGORY', 'PRODUCT', 'FREESHIP')) ),
    scope_id            BIGINT,
    discount_type       VARCHAR(10)    NOT NULL CHECK (discount_type IN ('FIXED', 'PERCENT') ),
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

CREATE INDEX idx_voucher_code ON voucher (code) WHERE deleted_at IS NULL;
CREATE INDEX idx_voucher_valid ON voucher (valid_from, valid_to);

CREATE TABLE flash_sale_campaign
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    start_time TIMESTAMPTZ  NOT NULL,
    end_time   TIMESTAMPTZ  NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'UPCOMING' CHECK (status IN ('UPCOMING', 'ACTIVE', 'ENDED'))
);
CREATE INDEX idx_flash_sale_campaign_status ON flash_sale_campaign (status);

CREATE TABLE flash_sale_item
(
    id             BIGSERIAL PRIMARY KEY,
    campaign_id    BIGINT         NOT NULL REFERENCES flash_sale_campaign (id),
    sku_id         BIGINT         NOT NULL REFERENCES sku (id),
    flash_price    NUMERIC(14, 2) NOT NULL,
    quantity_limit INTEGER        NOT NULL,
    UNIQUE (campaign_id, sku_id)
);
CREATE INDEX idx_flash_sale_item_sku ON flash_sale_item (sku_id);