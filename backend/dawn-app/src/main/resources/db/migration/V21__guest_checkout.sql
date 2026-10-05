-- Guest checkout (docs 13.5 lite): đơn + giỏ của khách vãng lai
ALTER TABLE orders ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS guest_email VARCHAR(255);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS guest_phone VARCHAR(20);
ALTER TABLE cart_item ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE cart_item ADD COLUMN IF NOT EXISTS guest_token VARCHAR(100);
-- Mỗi dòng giỏ thuộc đúng 1 chủ: user HOẶC guest
ALTER TABLE cart_item ADD CONSTRAINT chk_cart_owner CHECK (
    (user_id IS NOT NULL AND guest_token IS NULL)
    OR (user_id IS NULL AND guest_token IS NOT NULL)
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_cart_guest_sku
    ON cart_item (guest_token, sku_id) WHERE guest_token IS NOT NULL;
