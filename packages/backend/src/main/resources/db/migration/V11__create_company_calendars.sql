CREATE TABLE company_calendars (
    id UUID PRIMARY KEY,
    holiday_date DATE NOT NULL,
    holiday_name VARCHAR(100) NOT NULL,
    fiscal_year INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX idx_company_calendars_date ON company_calendars(holiday_date);
CREATE INDEX idx_company_calendars_year ON company_calendars(fiscal_year);
