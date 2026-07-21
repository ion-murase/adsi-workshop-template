CREATE TABLE paid_leave_balances (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    fiscal_year INTEGER NOT NULL,
    granted_days DECIMAL(4,1) NOT NULL,
    used_days DECIMAL(4,1) NOT NULL DEFAULT 0,
    carried_over_days DECIMAL(4,1) NOT NULL DEFAULT 0,
    expires_at DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_paid_leave_balances_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_paid_leave_balances_user_year UNIQUE (user_id, fiscal_year)
);
