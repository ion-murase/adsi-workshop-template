INSERT INTO sites (id, name, timezone, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000001', '本社', 'Asia/Tokyo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO departments (id, name, site_id, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000002', '開発部', '00000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (id, email, password_hash, name, role, primary_department_id, require_password_change, active, has_attendance_record, failed_login_attempts, created_at, updated_at, version)
VALUES ('00000000-0000-0000-0000-000000000010', 'test@example.com', '$2a$10$dummy', 'テストユーザ', 'GENERAL', '00000000-0000-0000-0000-000000000002', false, true, false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO users (id, email, password_hash, name, role, primary_department_id, require_password_change, active, has_attendance_record, failed_login_attempts, created_at, updated_at, version)
VALUES ('00000000-0000-0000-0000-000000000011', 'other@example.com', '$2a$10$dummy', '他ユーザ', 'GENERAL', '00000000-0000-0000-0000-000000000002', false, true, false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
