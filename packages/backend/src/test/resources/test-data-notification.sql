INSERT INTO sites (id, name, timezone, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000001', '本社', 'Asia/Tokyo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO departments (id, name, site_id, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000002', '開発部', '00000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (id, email, password_hash, name, role, primary_department_id, require_password_change, active, has_attendance_record, failed_login_attempts, created_at, updated_at, version)
VALUES ('00000000-0000-0000-0000-000000000010', 'test@example.com', '$2a$10$dummy', 'テストユーザ', 'GENERAL', '00000000-0000-0000-0000-000000000002', false, true, false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO notifications (id, recipient_id, type, title, message, reference_id, is_read, created_at)
VALUES
('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000010', 'APPLICATION_APPROVED', '申請が承認されました', '打刻修正申請が承認されました。', '00000000-0000-0000-0000-000000000201', false, '2026-07-01 09:00:00+09'),
('00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000010', 'OVERTIME_30', '残業30時間超過', '今月の残業時間が30時間を超えました。', NULL, false, '2026-07-02 10:00:00+09'),
('00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000010', 'APPLICATION_REJECTED', '申請が却下されました', '有給休暇申請が却下されました。', '00000000-0000-0000-0000-000000000202', true, '2026-06-28 15:00:00+09');
