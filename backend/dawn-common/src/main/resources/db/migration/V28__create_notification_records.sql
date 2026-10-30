-- V28: Notification tracking table
CREATE TABLE IF NOT EXISTS notification_records
(
    id            BIGSERIAL PRIMARY KEY,
    recipient_id  BIGINT,
    channel       VARCHAR(20)  NOT NULL,
    template_id   VARCHAR(100),
    variables     JSONB,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    sent_at       TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_notif_recipient_created ON notification_records (recipient_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notif_status ON notification_records (status) WHERE status = 'FAILED';
