-- V23: index tra cứu thông báo theo user (GET /notifications quét full khi bảng phình).
CREATE INDEX IF NOT EXISTS idx_notification_user ON notification (user_id);
