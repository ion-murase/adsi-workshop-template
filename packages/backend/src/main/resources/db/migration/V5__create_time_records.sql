CREATE TABLE time_records (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    work_date DATE NOT NULL,
    clock_in TIMESTAMP WITH TIME ZONE NOT NULL,
    clock_out TIMESTAMP WITH TIME ZONE,
    break_minutes INTEGER NOT NULL DEFAULT 60,
    work_minutes INTEGER,
    overtime_minutes INTEGER,
    night_minutes INTEGER,
    holiday_work_minutes INTEGER,
    is_holiday BOOLEAN NOT NULL DEFAULT false,
    timezone VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_time_records_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE UNIQUE INDEX idx_time_records_user_date ON time_records(user_id, work_date);
