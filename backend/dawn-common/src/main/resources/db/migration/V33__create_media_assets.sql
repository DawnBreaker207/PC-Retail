-- V33: Media assets metadata table for tracking uploaded files
CREATE TABLE IF NOT EXISTS media_assets
(
    id           BIGSERIAL PRIMARY KEY,
    object_key   VARCHAR(500)  NOT NULL UNIQUE,
    public_url   VARCHAR(1000) NOT NULL,
    content_type VARCHAR(100),
    size_bytes   BIGINT,
    purpose      VARCHAR(50),
    uploaded_by  BIGINT,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_media_uploaded_by ON media_assets (uploaded_by, created_at DESC);
