-- V4: In-house delivery + COD support

-- New SubOrderStatus values: READY_TO_SHIP, OUT_FOR_DELIVERY, DELIVERY_FAILED
-- (PostgreSQL enum requires ALTER TYPE; using VARCHAR column so no ALTER needed)

-- Add delivery fields to sub_order
ALTER TABLE sub_order
    ADD COLUMN IF NOT EXISTS shipping_method    VARCHAR(20)  NOT NULL DEFAULT 'INTERNAL',
    ADD COLUMN IF NOT EXISTS shipper_id         BIGINT,
    ADD COLUMN IF NOT EXISTS delivery_note      VARCHAR(500),
    ADD COLUMN IF NOT EXISTS delivery_attempted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS cod_collected_at   TIMESTAMP;

-- Index: shipper lookup their own orders
CREATE INDEX IF NOT EXISTS idx_sub_order_shipper
    ON sub_order (shipper_id)
    WHERE shipper_id IS NOT NULL;

-- Index: admin dashboard — orders ready to assign
CREATE INDEX IF NOT EXISTS idx_sub_order_status_ready
    ON sub_order (status)
    WHERE status IN ('READY_TO_SHIP', 'OUT_FOR_DELIVERY', 'DELIVERY_FAILED');

-- Add payment_method to orders (ONLINE | COD)
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS payment_method VARCHAR(20) NOT NULL DEFAULT 'ONLINE';
