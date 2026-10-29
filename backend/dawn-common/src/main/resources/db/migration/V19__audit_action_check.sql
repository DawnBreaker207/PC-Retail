-- Black-box: CHECK action chặn IMPORT_STOCK/TRANSITION_ORDER nên audit kho/đơn chưa từng ghi được
ALTER TABLE audit_logs DROP CONSTRAINT IF EXISTS audit_logs_action_check;
ALTER TABLE audit_logs ADD CONSTRAINT audit_logs_action_check
    CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT', 'IMPORT_STOCK', 'TRANSITION_ORDER'));
