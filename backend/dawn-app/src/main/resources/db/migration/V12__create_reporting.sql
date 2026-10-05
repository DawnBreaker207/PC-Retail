CREATE TABLE supplier (
id BIGSERIAL PRIMARY KEY,
name VARCHAR(255) NOT NULL,
contact_info JSONB,
tax_code VARCHAR(50),
created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
deleted_at TIMESTAMPTZ
);

CREATE TABLE purchase_order (
id BIGSERIAL PRIMARY KEY,
supplier_id BIGINT NOT NULL REFERENCES supplier(id),
status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','CONFIRMED','RECEIVED')),
total_cost NUMERIC(14,2) NOT NULL DEFAULT 0,
created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE purchase_order_item (
id BIGSERIAL PRIMARY KEY,
po_id BIGINT NOT NULL REFERENCES purchase_order(id),
sku_id BIGINT NOT NULL REFERENCES sku(id),
quantity INTEGER NOT NULL CHECK (quantity > 0),
unit_cost NUMERIC(14,2) NOT NULL
);

CREATE TABLE stock_take_session (
id BIGSERIAL PRIMARY KEY,
status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS','COMPLETED')),
started_by BIGINT NOT NULL REFERENCES users(id),
started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
completed_at TIMESTAMPTZ
);

CREATE TABLE stock_take_item (
id BIGSERIAL PRIMARY KEY,
session_id BIGINT NOT NULL REFERENCES stock_take_session(id),
sku_id BIGINT NOT NULL REFERENCES sku(id),
system_quantity INTEGER NOT NULL,
counted_quantity INTEGER NOT NULL
);
