-- V32: Wishlist items table
CREATE TABLE IF NOT EXISTS wishlist_items
(
    id       BIGSERIAL PRIMARY KEY,
    user_id  BIGINT      NOT NULL,
    sku_id   BIGINT      NOT NULL,
    added_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_wishlist_user_sku UNIQUE (user_id, sku_id)
);

CREATE INDEX IF NOT EXISTS idx_wishlist_user ON wishlist_items (user_id, added_at DESC);
CREATE INDEX IF NOT EXISTS idx_wishlist_sku ON wishlist_items (sku_id);
