-- Phase B: docs §13.x drift fixes
-- 1. inventory CHECK constraints
ALTER TABLE inventory ADD CONSTRAINT chk_inventory_stock_nonneg CHECK (stock_quantity >= 0);
ALTER TABLE inventory ADD CONSTRAINT chk_inventory_reserved_nonneg CHECK (reserved_quantity >= 0);
ALTER TABLE inventory ADD CONSTRAINT chk_inventory_reserved_lte_stock CHECK (reserved_quantity <= stock_quantity);

-- 2. inventory_transaction: reason + new types (keep old CHECK open via drop/recreate if exists)
ALTER TABLE inventory_transaction ADD COLUMN IF NOT EXISTS reason TEXT;

-- 3. review: shop reply + unique per sub_order+sku
ALTER TABLE review ADD COLUMN IF NOT EXISTS shop_reply TEXT;
ALTER TABLE review ADD COLUMN IF NOT EXISTS shop_reply_at TIMESTAMPTZ;
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_review_suborder_sku') THEN
    ALTER TABLE review ADD CONSTRAINT uq_review_suborder_sku UNIQUE (sub_order_id, sku_id);
  END IF;
END $$;

-- 4. notification: reference_type (docs) — body kept (code uses body)
ALTER TABLE notification ADD COLUMN IF NOT EXISTS reference_type VARCHAR(50);

-- 5. orders: composite idempotency (user_id, key) thay global unique
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_idempotency_key_key;
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_orders_user_idempotency') THEN
    ALTER TABLE orders ADD CONSTRAINT uq_orders_user_idempotency UNIQUE (user_id, idempotency_key);
  END IF;
END $$;

-- 6. order_item snapshot (chống giá 0đ khi SKU soft-delete)
ALTER TABLE order_item ADD COLUMN IF NOT EXISTS product_name VARCHAR(500);
ALTER TABLE order_item ADD COLUMN IF NOT EXISTS sku_code VARCHAR(100);
