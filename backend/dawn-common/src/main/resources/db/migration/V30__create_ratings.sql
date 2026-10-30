-- V30: Ratings table for dawn-rating service
CREATE TABLE IF NOT EXISTS ratings (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    sku_id     BIGINT       NOT NULL,
    order_id   BIGINT       NOT NULL,
    score      SMALLINT     NOT NULL CHECK (score BETWEEN 1 AND 5),
    comment    TEXT,
    image_urls JSONB                 DEFAULT '[]',
    status     VARCHAR(20)  NOT NULL DEFAULT 'PENDING_REVIEW',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_rating_user_sku_order UNIQUE (user_id, sku_id, order_id)
);

CREATE INDEX IF NOT EXISTS idx_ratings_sku_status ON ratings (sku_id, status);
CREATE INDEX IF NOT EXISTS idx_ratings_user ON ratings (user_id);
