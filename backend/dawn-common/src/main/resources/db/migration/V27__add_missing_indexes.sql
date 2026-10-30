-- V27: Add missing performance & correctness indexes identified in audit

-- payment: fast lookup by order, dedup by transaction reference
CREATE INDEX IF NOT EXISTS idx_payment_order_id ON payments (order_id);
CREATE UNIQUE INDEX IF NOT EXISTS uidx_payment_transaction_ref
    ON payments (transaction_ref)
    WHERE transaction_ref IS NOT NULL;

-- sku: GIN index on attributes and specs JSONB for key/value search
CREATE INDEX IF NOT EXISTS idx_sku_attributes_gin ON skus USING GIN (attributes);
CREATE INDEX IF NOT EXISTS idx_sku_specs_gin ON skus USING GIN (specs);

-- sku: trigram index for LIKE search (requires pg_trgm extension)
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_sku_name_trgm ON skus USING GIN (name gin_trgm_ops);

-- inventory: composite index for sku + warehouse lookups
CREATE INDEX IF NOT EXISTS idx_inventory_sku_warehouse ON inventory_items (sku_id, warehouse_id);

-- order_items: lookup items by sku_id (used by catalog soft-delete guard)
CREATE INDEX IF NOT EXISTS idx_order_items_sku_id ON order_items (sku_id);

-- outbox_events: relay query — pending events ordered by creation
CREATE INDEX IF NOT EXISTS idx_outbox_pending_created ON outbox_events (created_at ASC)
    WHERE published = false;

-- sub_orders: SAGA state lookup
CREATE INDEX IF NOT EXISTS idx_sub_orders_status ON sub_orders (status);
