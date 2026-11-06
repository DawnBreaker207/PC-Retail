-- Multi-warehouse: add warehouses table, migrate inventory to per-warehouse records

CREATE TABLE warehouses
(
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    address    TEXT,
    city       VARCHAR(100),
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Default warehouse for existing data
INSERT INTO warehouses (name, address, city, active)
VALUES ('Kho Chính', 'Hà Nội', 'Hà Nội', TRUE);

-- Add warehouse_id to inventory
ALTER TABLE inventory ADD COLUMN warehouse_id BIGINT;
UPDATE inventory SET warehouse_id = (SELECT id FROM warehouses WHERE name = 'Kho Chính' LIMIT 1);
ALTER TABLE inventory ALTER COLUMN warehouse_id SET NOT NULL;

-- Drop old unique on sku_id; add composite unique (sku_id, warehouse_id)
ALTER TABLE inventory DROP CONSTRAINT inventory_sku_id_key;
ALTER TABLE inventory ADD CONSTRAINT uq_inventory_sku_warehouse UNIQUE (sku_id, warehouse_id);

ALTER TABLE inventory ADD CONSTRAINT fk_inventory_warehouse
    FOREIGN KEY (warehouse_id) REFERENCES warehouses (id);

CREATE INDEX idx_inventory_warehouse ON inventory (warehouse_id);
