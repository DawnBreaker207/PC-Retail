-- V25: vector sở thích từ clickstream (dawn-ai) + gợi ý cấu hình theo ngân sách.
CREATE TABLE IF NOT EXISTS user_preference
(
    user_id           BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    preference_vector JSONB        NOT NULL DEFAULT '{}',
    average_price     NUMERIC(14, 2),
    event_count       BIGINT       NOT NULL DEFAULT 0,
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);
