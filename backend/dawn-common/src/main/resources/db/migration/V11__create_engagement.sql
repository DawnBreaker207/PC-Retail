CREATE TABLE review
(
    id           BIGSERIAL PRIMARY KEY,
    sub_order_id BIGINT      NOT NULL REFERENCES sub_order (id),
    sku_id       BIGINT      NOT NULL REFERENCES sku (id),
    user_id      BIGINT      NOT NULL REFERENCES users (id),
    rating       SMALLINT    NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment      TEXT,
    images       JSONB,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE notification
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users (id),
    type         VARCHAR(50)  NOT NULL,
    title        VARCHAR(255) NOT NULL,
    body         TEXT,
    reference_id BIGINT,
    is_read      BOOLEAN      NOT NULL DEFAULT false,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);