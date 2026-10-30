-- V26: Dead Letter Topic audit & replay table
CREATE TABLE IF NOT EXISTS dead_letter_records
(
    id                BIGSERIAL PRIMARY KEY,
    topic             VARCHAR(255) NOT NULL,
    message_key       VARCHAR(255),
    payload           TEXT         NOT NULL,
    exception_message TEXT,
    status            VARCHAR(50)  NOT NULL DEFAULT 'PENDING_REVIEW',
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    replayed_at       TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_dlt_records_status_created ON dead_letter_records (status, created_at DESC);
