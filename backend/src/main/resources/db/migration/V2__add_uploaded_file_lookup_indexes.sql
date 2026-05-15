CREATE INDEX idx_uploaded_files_uploaded_by
    ON uploaded_files (uploaded_by);

CREATE INDEX idx_uploaded_files_status
    ON uploaded_files (status);

CREATE INDEX idx_uploaded_files_uploaded_by_status
    ON uploaded_files (uploaded_by, status);
