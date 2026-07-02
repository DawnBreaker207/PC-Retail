CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    phone         VARCHAR(20),
    system_role   VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER' CHECK (system_role IN ('CUSTOMER', 'STAFF', 'ADMIN')),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    addresses     JSONB        NOT NULL DEFAULT '[]',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ
);

CREATE TABLE refresh_tokens
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id),
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_token_user ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_token_hash ON refresh_tokens (token_hash);

CREATE TABLE password_reset_tokens
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users (id),
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMPTZ  NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);