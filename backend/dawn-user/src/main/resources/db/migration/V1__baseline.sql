-- dawn-user: user profiles, wishlist, preferences

CREATE TABLE users
(
    id            BIGSERIAL    PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    phone         VARCHAR(20),
    system_role   VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER' CHECK (system_role IN ('CUSTOMER', 'STAFF', 'ADMIN')),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    addresses     JSONB        NOT NULL DEFAULT '[]',
    guest_email   VARCHAR(255),
    guest_phone   VARCHAR(20),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ
);

-- Seed staff fixture required by integration tests (staff flows, inventory actor FK)
INSERT INTO users (id, email, password_hash, full_name, system_role, status, addresses)
VALUES (3, 'staff@test.vn', 'x', 'Staff', 'STAFF', 'ACTIVE', '[]')
ON CONFLICT (id) DO NOTHING;
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users), true);

CREATE TABLE wishlist_items
(
    id       BIGSERIAL   PRIMARY KEY,
    user_id  BIGINT      NOT NULL,
    sku_id   BIGINT      NOT NULL,
    added_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_wishlist_user_sku UNIQUE (user_id, sku_id)
);
CREATE INDEX idx_wishlist_items_user ON wishlist_items (user_id, added_at DESC);
CREATE INDEX idx_wishlist_items_sku  ON wishlist_items (sku_id);

CREATE TABLE user_preference
(
    user_id           BIGINT         PRIMARY KEY,
    preference_vector JSONB          NOT NULL DEFAULT '{}',
    average_price     NUMERIC(14, 2),
    event_count       BIGINT         NOT NULL DEFAULT 0,
    updated_at        TIMESTAMPTZ    NOT NULL DEFAULT now()
);
