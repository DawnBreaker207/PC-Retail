CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE product_embedding
(
    id             BIGSERIAL PRIMARY KEY,
    sku_id         BIGINT      NOT NULL UNIQUE REFERENCES sku (id),
    embedding_text TEXT        NOT NULL,
    model_version  VARCHAR(50) NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_product_embedding_hnsw
    ON product_embedding
        USING hnsw (embedding vector_cosine_ops);