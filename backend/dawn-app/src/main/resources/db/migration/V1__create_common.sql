CREATE TABLE audit_logs
(
    id            BIGSERIAL PRIMARY KEY,
    actor_id      BIGINT,
    actor_role    VARCHAR(30),
    action        VARCHAR(20)  NOT NULL CHECK ( action IN ('CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT') ),
    entity_type   VARCHAR(100) NOT NULL,
    entity_id     BIGINT,
    old_value     JSONB,
    new_value     JSONB,
    ip_address    VARCHAR(45),
    tier          SMALLINT     NOT NULL DEFAULT 2,
    status        VARCHAR(10)  NOT NULL DEFAULT 'SUCCESS' CHECK ( status IN ('SUCCESS', 'FAILED') ),
    error_message TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_log_actor ON audit_logs (actor_id);
CREATE INDEX idx_audit_log_created_at ON audit_logs (created_at);