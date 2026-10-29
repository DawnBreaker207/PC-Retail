-- V24: Transactional Outbox — event ghi cùng transaction nghiệp vụ, relay đẩy Kafka sau.
CREATE TABLE IF NOT EXISTS outbox_events (
    id             BIGSERIAL PRIMARY KEY,
    topic          VARCHAR(80)  NOT NULL,
    aggregate_type VARCHAR(40)  NOT NULL,
    aggregate_id   BIGINT       NOT NULL,
    event_type     VARCHAR(80)  NOT NULL,
    payload        JSONB        NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at   TIMESTAMPTZ,
    attempts       INT          NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_outbox_pending ON outbox_events (published_at, id) WHERE published_at IS NULL;
