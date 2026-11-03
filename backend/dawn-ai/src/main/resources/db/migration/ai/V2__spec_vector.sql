-- Migrate from LLM embedding (1536-dim) to spec-based vector (13-dim).
-- Safe to truncate: embeddings are regenerated on demand via /ai/embeddings/generate.
TRUNCATE TABLE product_embedding;

ALTER TABLE product_embedding DROP COLUMN embedding;
ALTER TABLE product_embedding ADD COLUMN embedding vector(13);

DROP INDEX IF EXISTS idx_product_embedding_hnsw;
CREATE INDEX idx_product_embedding_hnsw
    ON product_embedding USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);
