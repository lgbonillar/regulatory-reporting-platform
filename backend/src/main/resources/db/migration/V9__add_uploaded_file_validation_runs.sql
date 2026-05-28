CREATE TABLE uploaded_file_validation_runs (
    id UUID PRIMARY KEY,
    uploaded_file_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    source VARCHAR(30) NOT NULL,
    summary_message TEXT,
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_uploaded_file_validation_runs_uploaded_file
        FOREIGN KEY (uploaded_file_id)
            REFERENCES uploaded_files (id)
);

CREATE INDEX idx_uploaded_file_validation_runs_uploaded_file_id
    ON uploaded_file_validation_runs (uploaded_file_id);

CREATE INDEX idx_uploaded_file_validation_runs_created_at
    ON uploaded_file_validation_runs (created_at);

CREATE TABLE uploaded_file_findings (
    id UUID PRIMARY KEY,
    validation_run_id UUID NOT NULL,
    uploaded_file_id UUID NOT NULL,
    severity VARCHAR(30) NOT NULL,
    scope VARCHAR(50) NOT NULL,
    code VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    sheet_name VARCHAR(150),
    row_number INTEGER,
    column_name VARCHAR(150),
    field_name VARCHAR(150),
    rejected_value TEXT,
    expected_value TEXT,
    actual_value TEXT,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_uploaded_file_findings_validation_run
        FOREIGN KEY (validation_run_id)
            REFERENCES uploaded_file_validation_runs (id),

    CONSTRAINT fk_uploaded_file_findings_uploaded_file
        FOREIGN KEY (uploaded_file_id)
            REFERENCES uploaded_files (id)
);

CREATE INDEX idx_uploaded_file_findings_uploaded_file_id
    ON uploaded_file_findings (uploaded_file_id);

CREATE INDEX idx_uploaded_file_findings_validation_run_id
    ON uploaded_file_findings (validation_run_id);

CREATE INDEX idx_uploaded_file_findings_severity
    ON uploaded_file_findings (severity);

CREATE INDEX idx_uploaded_file_findings_scope
    ON uploaded_file_findings (scope);
