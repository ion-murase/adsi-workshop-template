CREATE TABLE leave_request_details (
    application_id UUID PRIMARY KEY,
    leave_date DATE NOT NULL,
    leave_type VARCHAR(10) NOT NULL,
    CONSTRAINT fk_leave_request_details_application FOREIGN KEY (application_id) REFERENCES applications(id)
);
