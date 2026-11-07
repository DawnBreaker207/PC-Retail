-- V5: Bank info for manual refund, V4 status additions

-- Add bank info columns to refund table
ALTER TABLE refund
    ADD COLUMN IF NOT EXISTS bank_account   VARCHAR(30),
    ADD COLUMN IF NOT EXISTS bank_name      VARCHAR(100),
    ADD COLUMN IF NOT EXISTS account_holder VARCHAR(100),
    ADD COLUMN IF NOT EXISTS processed_by   BIGINT;

-- Add bank info to return_request (collected at submit time)
ALTER TABLE return_request
    ADD COLUMN IF NOT EXISTS bank_account   VARCHAR(30),
    ADD COLUMN IF NOT EXISTS bank_name      VARCHAR(100),
    ADD COLUMN IF NOT EXISTS account_holder VARCHAR(100);

-- Fix status CHECK to include in-house delivery states added in V4
ALTER TABLE sub_order DROP CONSTRAINT IF EXISTS sub_order_status_check;
ALTER TABLE sub_order ADD CONSTRAINT sub_order_status_check CHECK (status IN (
    'PENDING_PAYMENT', 'AWAITING_INVENTORY', 'PAID', 'PROCESSING',
    'READY_TO_SHIP', 'OUT_FOR_DELIVERY', 'DELIVERY_FAILED',
    'SHIPPED', 'READY_FOR_PICKUP', 'DELIVERED',
    'RETURN_REQUESTED', 'RETURNED',
    'EXPIRED', 'CANCELLING', 'CANCELLED', 'CANCELLED_OUT_OF_STOCK'
));
