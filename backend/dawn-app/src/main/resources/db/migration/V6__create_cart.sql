CREATE TABLE cart_item
(
    id       BIGSERIAL PRIMARY KEY,
    user_id  BIGINT      NOT NULL REFERENCES users (id),
    sku_id   BIGINT      NOT NULL REFERENCES sku (id),
    quantity INTEGER     NOT NULL CHECK (quantity > 0 ),
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version  BIGINT      NOT NULL DEFAULT 0,
    UNIQUE (user_id, sku_id)
);