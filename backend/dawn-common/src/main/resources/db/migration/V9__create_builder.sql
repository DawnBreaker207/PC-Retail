CREATE TABLE build_template (
id BIGSERIAL PRIMARY KEY,
name VARCHAR(255) NOT NULL,
description TEXT,
thumbnail_url VARCHAR(500),
status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
created_by BIGINT NOT NULL REFERENCES users(id),
created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_build_template_status ON build_template (status);

CREATE TABLE build_template_slot (
id BIGSERIAL PRIMARY KEY,
template_id BIGINT NOT NULL REFERENCES build_template(id) ON DELETE CASCADE,
slot_type VARCHAR(20) NOT NULL CHECK (slot_type IN (
    'CPU','MAINBOARD','RAM','GPU','PSU','CASE','STORAGE','COOLER',
    'MONITOR','KEYBOARD','MOUSE','HEADSET'
)),
default_sku_id BIGINT REFERENCES sku(id),
allowed_sku_ids JSONB,
is_removable BOOLEAN NOT NULL DEFAULT false,
UNIQUE (template_id, slot_type)
);
CREATE INDEX idx_build_template_slot_template ON build_template_slot (template_id);

CREATE TABLE wishlist_item (
id BIGSERIAL PRIMARY KEY,
user_id BIGINT NOT NULL REFERENCES users(id),
sku_id BIGINT NOT NULL REFERENCES sku(id),
created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
UNIQUE (user_id, sku_id)
);
CREATE INDEX idx_wishlist_user ON wishlist_item (user_id);
