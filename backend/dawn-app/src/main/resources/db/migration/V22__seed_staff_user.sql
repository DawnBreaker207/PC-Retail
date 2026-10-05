-- V22: seed staff fixture cho E2E/staff flows (auth.helper STAFF = id 3).
-- Nhip kho bù khi hủy đơn PAID ghi created_by = actor → FK users, thiếu row là 500.
INSERT INTO users (id, email, password_hash, full_name, system_role, status, addresses)
VALUES (3, 'staff@test.vn', 'x', 'Staff', 'STAFF', 'ACTIVE', '[]'::jsonb)
ON CONFLICT (id) DO NOTHING;

-- Insert tay id tường minh không nhảy sequence → fix để user đăng ký sau không đụng id 3
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users), true);
