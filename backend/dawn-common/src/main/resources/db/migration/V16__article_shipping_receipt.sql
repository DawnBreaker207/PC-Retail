-- V16: Article + Shipping + StockReceipt (V9/V10/V12 đã có builder/return/reporting)
CREATE TABLE IF NOT EXISTS article (
id BIGSERIAL PRIMARY KEY,
title VARCHAR(500) NOT NULL,
slug VARCHAR(500) NOT NULL UNIQUE,
content TEXT,
thumbnail_url VARCHAR(500),
category VARCHAR(30) NOT NULL DEFAULT 'NEWS',
status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','PUBLISHED','HIDDEN')),
author_id BIGINT REFERENCES users(id),
published_at TIMESTAMPTZ,
template_id BIGINT,
deleted_at TIMESTAMPTZ,
created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS shipping_zone (
id BIGSERIAL PRIMARY KEY,
zone_code VARCHAR(50) NOT NULL UNIQUE,
match_city VARCHAR(100),
fee NUMERIC(14,2) NOT NULL,
is_active BOOLEAN NOT NULL DEFAULT true,
created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
INSERT INTO shipping_zone (zone_code, match_city, fee, is_active) VALUES
('DEFAULT', NULL, 30000, true)
ON CONFLICT (zone_code) DO NOTHING;

CREATE TABLE IF NOT EXISTS stock_receipt (
id BIGSERIAL PRIMARY KEY,
status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
created_by BIGINT REFERENCES users(id),
confirmed_at TIMESTAMPTZ,
created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS stock_receipt_item (
id BIGSERIAL PRIMARY KEY,
receipt_id BIGINT NOT NULL REFERENCES stock_receipt(id) ON DELETE CASCADE,
sku_id BIGINT NOT NULL REFERENCES sku(id),
quantity INTEGER NOT NULL CHECK (quantity > 0),
unit_cost NUMERIC(14,2),
created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
