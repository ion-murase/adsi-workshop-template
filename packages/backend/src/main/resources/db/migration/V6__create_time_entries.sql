CREATE TABLE time_entries (
    id UUID PRIMARY KEY,
    time_record_id UUID NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_time_entries_record FOREIGN KEY (time_record_id) REFERENCES time_records(id)
);

CREATE INDEX idx_time_entries_record ON time_entries(time_record_id);
