CREATE TABLE clock_fix_details (
    application_id UUID PRIMARY KEY,
    target_date DATE NOT NULL,
    corrected_clock_in TIMESTAMP WITH TIME ZONE,
    corrected_clock_out TIMESTAMP WITH TIME ZONE,
    reason TEXT NOT NULL,
    CONSTRAINT fk_clock_fix_details_application FOREIGN KEY (application_id) REFERENCES applications(id)
);
