UPDATE processing_jobs
SET status = CASE status
        WHEN 'PENDING' THEN 'PENDING_EXECUTION'
        WHEN 'FAILED' THEN 'PROCESSING_FAILED'
        WHEN 'COMPLETED' THEN 'AWAITING_APPROVAL'
        ELSE status
    END
WHERE status IN ('PENDING', 'FAILED', 'COMPLETED');

ALTER TABLE processing_jobs
    ADD COLUMN triggered_by_user_id UUID,
    ADD COLUMN triggered_at TIMESTAMP,
    ADD COLUMN processing_completed_at TIMESTAMP,
    ADD COLUMN failure_reason VARCHAR(2000),

    ADD COLUMN approved_by_user_id UUID,
    ADD COLUMN approved_at TIMESTAMP,

    ADD COLUMN rejected_by_user_id UUID,
    ADD COLUMN rejected_at TIMESTAMP,
    ADD COLUMN rejection_reason VARCHAR(1000),

    ADD COLUMN revoked_by_user_id UUID,
    ADD COLUMN revoked_at TIMESTAMP,
    ADD COLUMN revocation_reason VARCHAR(1000);

ALTER TABLE processing_jobs
    ADD CONSTRAINT fk_processing_jobs_triggered_by_user
        FOREIGN KEY (triggered_by_user_id) REFERENCES users (id),

    ADD CONSTRAINT fk_processing_jobs_approved_by_user
        FOREIGN KEY (approved_by_user_id) REFERENCES users (id),

    ADD CONSTRAINT fk_processing_jobs_rejected_by_user
        FOREIGN KEY (rejected_by_user_id) REFERENCES users (id),

    ADD CONSTRAINT fk_processing_jobs_revoked_by_user
        FOREIGN KEY (revoked_by_user_id) REFERENCES users (id),

    ADD CONSTRAINT ck_processing_jobs_status
        CHECK (status IN (
                'PENDING_EXECUTION',
                'PROCESSING',
                'PROCESSING_FAILED',
                'AWAITING_APPROVAL',
                'APPROVED',
                'REJECTED',
                'REVOKED'
            ));

CREATE TABLE processing_job_status_history (
    id UUID PRIMARY KEY,
    processing_job_id UUID NOT NULL,

    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,

    transition_source VARCHAR(20) NOT NULL,
    transitioned_by_user_id UUID,

    reason VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_processing_job_history_job
       FOREIGN KEY (processing_job_id) REFERENCES processing_jobs (id),

    CONSTRAINT fk_processing_job_history_user
       FOREIGN KEY (transitioned_by_user_id) REFERENCES users (id),

    CONSTRAINT ck_processing_job_history_previous_status
        CHECK (
            previous_status IS NULL OR previous_status IN (
                    'PENDING_EXECUTION',
                    'PROCESSING',
                    'PROCESSING_FAILED',
                    'AWAITING_APPROVAL',
                    'APPROVED',
                    'REJECTED',
                    'REVOKED'
                )
           ),

    CONSTRAINT ck_processing_job_history_new_status
        CHECK (new_status IN (
            'PENDING_EXECUTION',
            'PROCESSING',
            'PROCESSING_FAILED',
            'AWAITING_APPROVAL',
            'APPROVED',
            'REJECTED',
            'REVOKED'
           )),

    CONSTRAINT ck_processing_job_history_transition_source
       CHECK (transition_source IN ('USER', 'SYSTEM')),

    CONSTRAINT ck_processing_job_history_actor
       CHECK (
           (transition_source = 'USER' AND transitioned_by_user_id IS NOT NULL)
               OR
           (transition_source = 'SYSTEM' AND transitioned_by_user_id IS NULL)
           )
);

CREATE INDEX idx_processing_job_history_job_created_at
    ON processing_job_status_history (processing_job_id, created_at);

CREATE INDEX idx_processing_job_history_user
    ON processing_job_status_history (transitioned_by_user_id);

INSERT INTO processing_job_status_history (
    id,
    processing_job_id,
    previous_status,
    new_status,
    transition_source,
    transitioned_by_user_id,
    reason,
    created_at
)
SELECT
    gen_random_uuid(),
    job.id,
    NULL,
    job.status,
    'SYSTEM',
    NULL,
    'Initial workflow state migrated from existing processing job',
    job.created_at
FROM processing_jobs job;
