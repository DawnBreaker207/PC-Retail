CREATE TABLE brand
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    logo_url   VARCHAR(255),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE TABLE category
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    slug       VARCHAR(255) NOT NULL UNIQUE,
    parent_id  BIGINT REFERENCES category (id),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_category_parent ON category (parent_id);


CREATE TABLE sku
(
    id           BIGSERIAL PRIMARY KEY,
    sku_code     VARCHAR(100)   NOT NULL UNIQUE,
    name         VARCHAR(500)   NOT NULL,
    slug         VARCHAR(500)   NOT NULL UNIQUE,
    description  TEXT,
    brand_id     BIGINT REFERENCES brand (id),
    category_id  BIGINT         NOT NULL REFERENCES category (id),
    product_type VARCHAR(20)    NOT NULL DEFAULT 'PHYSICAL' CHECK ( product_type IN ('PHYSICAL', 'SERVICE') ),
    status       VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE' CHECK ( status IN ('ACTIVE', 'HIDDEN', 'DISCONTINUED')),
    price        NUMERIC(14, 2) NOT NULL,
    attributes   JSONB          NOT NULL DEFAULT '{}',
    specs        JSONB          NOT NULL DEFAULT '{}',
    image_urls   JSONB          NOT NULL DEFAULT '[]',
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ
);

CREATE INDEX idx_sku_brand ON sku (brand_id);
CREATE INDEX idx_sku_category ON sku (category_id);
CREATE INDEX idx_sku_status ON sku (status) WHERE deleted_at IS NULL;
CREATE INDEX idx_sku_specs_gin ON sku USING GIN (specs);