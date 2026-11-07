-- V6: delivery_attempts counter for auto-cancel after max failed deliveries
ALTER TABLE sub_order
    ADD COLUMN IF NOT EXISTS delivery_attempts INT NOT NULL DEFAULT 0;
