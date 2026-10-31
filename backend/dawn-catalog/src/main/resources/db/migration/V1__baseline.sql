-- dawn-catalog: brands, categories, SKUs, builder templates, ratings, media

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE brand
(
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    logo_url   VARCHAR(255),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE TABLE category
(
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    slug       VARCHAR(255) NOT NULL UNIQUE,
    parent_id  BIGINT       REFERENCES category (id),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);
CREATE INDEX idx_category_parent ON category (parent_id);

CREATE TABLE sku
(
    id           BIGSERIAL      PRIMARY KEY,
    sku_code     VARCHAR(100)   NOT NULL UNIQUE,
    name         VARCHAR(500)   NOT NULL,
    slug         VARCHAR(500)   NOT NULL UNIQUE,
    description  TEXT,
    brand_id     BIGINT         REFERENCES brand (id),
    category_id  BIGINT         NOT NULL REFERENCES category (id),
    product_type VARCHAR(20)    NOT NULL DEFAULT 'PHYSICAL' CHECK (product_type IN ('PHYSICAL', 'SERVICE')),
    status       VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'HIDDEN', 'DISCONTINUED')),
    price        NUMERIC(14, 2) NOT NULL,
    attributes   JSONB          NOT NULL DEFAULT '{}',
    specs        JSONB          NOT NULL DEFAULT '{}',
    image_urls   JSONB          NOT NULL DEFAULT '[]',
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ
);
CREATE INDEX idx_sku_brand       ON sku (brand_id);
CREATE INDEX idx_sku_category    ON sku (category_id);
CREATE INDEX idx_sku_status      ON sku (status) WHERE deleted_at IS NULL;
CREATE INDEX idx_sku_specs_gin   ON sku USING GIN (specs);
CREATE INDEX idx_sku_attrs_gin   ON sku USING GIN (attributes);
CREATE INDEX idx_sku_name_trgm   ON sku USING GIN (name gin_trgm_ops);

CREATE TABLE build_template
(
    id            BIGSERIAL    PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    thumbnail_url VARCHAR(500),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_by    BIGINT       NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_build_template_status ON build_template (status);

CREATE TABLE build_template_slot
(
    id              BIGSERIAL   PRIMARY KEY,
    template_id     BIGINT      NOT NULL REFERENCES build_template (id) ON DELETE CASCADE,
    slot_type       VARCHAR(20) NOT NULL CHECK (slot_type IN (
                        'CPU', 'MAINBOARD', 'RAM', 'GPU', 'PSU', 'CASE',
                        'STORAGE', 'COOLER', 'MONITOR', 'KEYBOARD', 'MOUSE', 'HEADSET')),
    default_sku_id  BIGINT,
    allowed_sku_ids JSONB,
    is_removable    BOOLEAN     NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (template_id, slot_type)
);
CREATE INDEX idx_build_template_slot_template ON build_template_slot (template_id);

CREATE TABLE ratings
(
    id         BIGSERIAL   PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    sku_id     BIGINT      NOT NULL,
    order_id   BIGINT      NOT NULL,
    score      SMALLINT    NOT NULL CHECK (score BETWEEN 1 AND 5),
    comment    TEXT,
    image_urls JSONB                DEFAULT '[]',
    status     VARCHAR(20) NOT NULL DEFAULT 'PENDING_REVIEW',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_rating_user_sku_order UNIQUE (user_id, sku_id, order_id)
);
CREATE INDEX idx_ratings_sku_status ON ratings (sku_id, status);
CREATE INDEX idx_ratings_user       ON ratings (user_id);

CREATE TABLE media_assets
(
    id           BIGSERIAL     PRIMARY KEY,
    object_key   VARCHAR(500)  NOT NULL UNIQUE,
    public_url   VARCHAR(1000) NOT NULL,
    content_type VARCHAR(100),
    size_bytes   BIGINT,
    purpose      VARCHAR(50),
    uploaded_by  BIGINT,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_media_uploaded_by ON media_assets (uploaded_by, created_at DESC);
