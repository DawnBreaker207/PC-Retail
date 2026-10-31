-- ============================================================
-- dawn-auth: auth domain + shared infrastructure tables
-- (auth starts first; shared tables created here for all services)
-- ============================================================

-- Auth domain
CREATE TABLE user_credentials
(
    id            BIGSERIAL    PRIMARY KEY,
    user_id       BIGINT       NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    roles         TEXT[]       NOT NULL DEFAULT '{}',
    enabled       BOOLEAN      NOT NULL DEFAULT true,
    locked        BOOLEAN      NOT NULL DEFAULT false,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE refresh_tokens
(
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

CREATE TABLE otp_codes
(
    id         BIGSERIAL    PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    code_hash  VARCHAR(255) NOT NULL,
    purpose    VARCHAR(50)  NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_otp_email_purpose ON otp_codes (email, purpose);

-- ============================================================
-- Shared infrastructure (created here; used by all services)
-- ============================================================

CREATE TABLE audit_logs
(
    id            BIGSERIAL    PRIMARY KEY,
    actor_id      BIGINT,
    actor_role    VARCHAR(30),
    action        VARCHAR(20)  NOT NULL CHECK (action IN (
                      'CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT',
                      'IMPORT_STOCK', 'TRANSITION_ORDER')),
    entity_type   VARCHAR(100) NOT NULL,
    entity_id     BIGINT,
    old_value     JSONB,
    new_value     JSONB,
    ip_address    VARCHAR(45),
    tier          SMALLINT     NOT NULL DEFAULT 2,
    status        VARCHAR(10)  NOT NULL DEFAULT 'SUCCESS' CHECK (status IN ('SUCCESS', 'FAILED')),
    error_message TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_log_entity     ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_log_actor      ON audit_logs (actor_id);
CREATE INDEX idx_audit_log_created_at ON audit_logs (created_at);

CREATE TABLE outbox_events
(
    id             BIGSERIAL   PRIMARY KEY,
    topic          VARCHAR(80) NOT NULL,
    aggregate_type VARCHAR(40) NOT NULL,
    aggregate_id   BIGINT      NOT NULL,
    event_type     VARCHAR(80) NOT NULL,
    payload        JSONB       NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at   TIMESTAMPTZ,
    attempts       INT         NOT NULL DEFAULT 0
);
CREATE INDEX idx_outbox_pending ON outbox_events (created_at ASC) WHERE published_at IS NULL;

CREATE TABLE dead_letter_records
(
    id                BIGSERIAL    PRIMARY KEY,
    topic             VARCHAR(255) NOT NULL,
    message_key       VARCHAR(255),
    payload           TEXT         NOT NULL,
    exception_message TEXT,
    status            VARCHAR(50)  NOT NULL DEFAULT 'PENDING_REVIEW',
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    replayed_at       TIMESTAMPTZ
);
CREATE INDEX idx_dlt_status_created ON dead_letter_records (status, created_at DESC);
