CREATE TABLE uploaded_files (
    id UUID PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    content_type VARCHAR(150),
    file_size BIGINT NOT NULL,
    checksum VARCHAR(64) NOT NULL,
    status VARCHAR(30) NOT NULL,
    uploaded_by VARCHAR(100) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT uk_uploaded_files_user_filename
        UNIQUE (uploaded_by, original_filename)
);

CREATE TABLE processing_jobs (
     id UUID PRIMARY KEY,
     uploaded_file_id UUID NOT NULL UNIQUE,
     status VARCHAR(30) NOT NULL,
     message VARCHAR(500),
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP,

     CONSTRAINT fk_processing_jobs_uploaded_file
         FOREIGN KEY (uploaded_file_id)
             REFERENCES uploaded_files (id)
);

CREATE INDEX idx_processing_jobs_report_file_id
    ON processing_jobs (uploaded_file_id);

CREATE INDEX idx_processing_jobs_status
    ON processing_jobs (status);