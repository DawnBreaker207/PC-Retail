-- V3: Add warehouse_id to sub_order so inventory ops target the correct warehouse
--     Add index on tracking_code for GHN webhook lookups

ALTER TABLE sub_order ADD COLUMN warehouse_id BIGINT;
CREATE INDEX idx_sub_order_warehouse     ON sub_order (warehouse_id);
CREATE INDEX idx_sub_order_tracking_code ON sub_order (tracking_code) WHERE tracking_code IS NOT NULL;
