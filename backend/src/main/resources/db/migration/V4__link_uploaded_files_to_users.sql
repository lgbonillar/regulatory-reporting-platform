ALTER TABLE uploaded_files
    ADD COLUMN uploaded_by_user_id UUID;

UPDATE uploaded_files uploaded_file
SET uploaded_by_user_id = app_user.id
FROM users app_user
WHERE app_user.username = uploaded_file.uploaded_by;

DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM uploaded_files
            WHERE uploaded_by_user_id IS NULL
        ) THEN
            RAISE EXCEPTION 'Cannot migrate uploaded_files: uploaded_by has no matching user';
        END IF;
    END $$;

ALTER TABLE uploaded_files
    ALTER COLUMN uploaded_by_user_id SET NOT NULL;

ALTER TABLE uploaded_files
    ADD CONSTRAINT fk_uploaded_files_uploaded_by_user
        FOREIGN KEY (uploaded_by_user_id) REFERENCES users (id);

ALTER TABLE uploaded_files
    DROP CONSTRAINT uk_uploaded_files_user_filename;

DROP INDEX idx_uploaded_files_uploaded_by;
DROP INDEX idx_uploaded_files_uploaded_by_status;

ALTER TABLE uploaded_files
    ADD CONSTRAINT uk_uploaded_files_user_filename
        UNIQUE (uploaded_by_user_id, original_filename);

CREATE INDEX idx_uploaded_files_uploaded_by_user_id
    ON uploaded_files (uploaded_by_user_id);

CREATE INDEX idx_uploaded_files_uploaded_by_user_id_status
    ON uploaded_files (uploaded_by_user_id, status);

ALTER TABLE uploaded_files
    DROP COLUMN uploaded_by;
