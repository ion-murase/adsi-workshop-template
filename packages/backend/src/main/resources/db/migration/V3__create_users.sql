CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    primary_department_id UUID NOT NULL,
    require_password_change BOOLEAN NOT NULL DEFAULT true,
    active BOOLEAN NOT NULL DEFAULT true,
    has_attendance_record BOOLEAN NOT NULL DEFAULT false,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_users_primary_department FOREIGN KEY (primary_department_id) REFERENCES departments(id)
);

CREATE UNIQUE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_primary_department_id ON users(primary_department_id);
CREATE INDEX idx_users_active ON users(active);
