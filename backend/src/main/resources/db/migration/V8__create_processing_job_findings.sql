CREATE TABLE processing_job_findings (
    id UUID PRIMARY KEY,
    processing_job_id UUID NOT NULL,

    severity VARCHAR(20) NOT NULL,
    scope VARCHAR(40) NOT NULL,

    code VARCHAR(100) NOT NULL,
    message VARCHAR(1000) NOT NULL,

    sheet_name VARCHAR(150),
    row_number INTEGER,
    column_name VARCHAR(150),
    field_name VARCHAR(150),

    rejected_value VARCHAR(1000),
    expected_value VARCHAR(1000),
    actual_value VARCHAR(1000),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_processing_job_findings_job
        FOREIGN KEY (processing_job_id) REFERENCES processing_jobs (id),

    CONSTRAINT ck_processing_job_findings_severity
        CHECK (severity IN ('ERROR', 'WARNING', 'INFO')),

    CONSTRAINT ck_processing_job_findings_scope
        CHECK (scope IN (
            'FILE_STRUCTURE',
            'SHEET_STRUCTURE',
            'COLUMN_STRUCTURE',
            'ROW_DATA',
            'BUSINESS_RULE',
            'CROSS_FILE_VALIDATION',
            'SYSTEM'
    ))
);

CREATE INDEX idx_processing_job_findings_job_created_at
    ON processing_job_findings (processing_job_id, created_at);

CREATE INDEX idx_processing_job_findings_job_severity
    ON processing_job_findings (processing_job_id, severity);

CREATE INDEX idx_processing_job_findings_code
    ON processing_job_findings (code);
