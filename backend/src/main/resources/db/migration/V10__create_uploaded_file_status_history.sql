CREATE TABLE uploaded_file_status_history (
    id UUID PRIMARY KEY,
    uploaded_file_id UUID NOT NULL,
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    transition_source VARCHAR(30) NOT NULL,
    transitioned_by_user_id UUID,
    reason VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_uploaded_file_status_history_uploaded_file
        FOREIGN KEY (uploaded_file_id)
            REFERENCES uploaded_files (id),

    CONSTRAINT fk_uploaded_file_status_history_transitioned_by_user
        FOREIGN KEY (transitioned_by_user_id)
            REFERENCES users (id)
);

CREATE INDEX idx_uploaded_file_status_history_uploaded_file_id
    ON uploaded_file_status_history (uploaded_file_id);

CREATE INDEX idx_uploaded_file_status_history_created_at
    ON uploaded_file_status_history (created_at);
