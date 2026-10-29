CREATE TABLE return_request (
id BIGSERIAL PRIMARY KEY,
sub_order_id BIGINT NOT NULL REFERENCES sub_order(id),
requested_by BIGINT NOT NULL REFERENCES users(id),
reason TEXT NOT NULL,
status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED' CHECK (status IN ('REQUESTED','APPROVED','REJECTED','REFUNDED','DISPUTED')),
images JSONB NOT NULL DEFAULT '[]',
reviewed_by BIGINT REFERENCES users(id),
reviewed_at TIMESTAMPTZ,
disputed_at TIMESTAMPTZ,
dispute_resolved_by BIGINT REFERENCES users(id),
dispute_resolution VARCHAR(20) CHECK (dispute_resolution IN ('UPHOLD_REJECT','OVERTURN_APPROVE')),
dispute_note TEXT,
created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_return_request_sub_order ON return_request (sub_order_id);
CREATE INDEX idx_return_request_status ON return_request (status);

CREATE TABLE refund (
id BIGSERIAL PRIMARY KEY,
return_request_id BIGINT REFERENCES return_request(id),
sub_order_id BIGINT NOT NULL REFERENCES sub_order(id),
amount NUMERIC(14,2) NOT NULL,
status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','PROCESSING','COMPLETED','FAILED')),
refund_method VARCHAR(20) NOT NULL,
processed_at TIMESTAMPTZ,
created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_refund_sub_order ON refund (sub_order_id);
