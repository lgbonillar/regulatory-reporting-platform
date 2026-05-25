BEGIN;

-- Password: password
-- BCrypt hash from Spring Security demos.
WITH demo_users AS (
    SELECT *
    FROM (VALUES
        ('00000000-0000-0000-0000-000000000001'::uuid, 'analyst01', 'analyst01@example.local', 'Analyst 01', 'ANALYST'),
        ('00000000-0000-0000-0000-000000000011'::uuid, 'analyst02', 'analyst02@example.local', 'Analyst 02', 'ANALYST'),
        ('00000000-0000-0000-0000-000000000012'::uuid, 'analyst03', 'analyst03@example.local', 'Analyst 03', 'ANALYST'),
        ('00000000-0000-0000-0000-000000000002'::uuid, 'admin01', 'admin01@example.local', 'Administrator 01', 'ADMINISTRATOR'),
        ('00000000-0000-0000-0000-000000000021'::uuid, 'admin02', 'admin02@example.local', 'Administrator 02', 'ADMINISTRATOR'),
        ('00000000-0000-0000-0000-000000000031'::uuid, 'auditor01', 'auditor01@example.local', 'Auditor 01', 'AUDITOR'),
        ('00000000-0000-0000-0000-000000000041'::uuid, 'root01', 'root01@example.local', 'Root 01', 'ROOT')
    ) AS t(id, username, email, display_name, role_code)
)
INSERT INTO users (
    id,
    username,
    email,
    display_name,
    password_hash,
    must_change_password,
    status
)
SELECT
    id,
    username,
    email,
    display_name,
    '$2a$10$PVfqf6KY/a5Y8l/7Hudb/.XHxdxo72cvBIWhX5V1utvNnG3DnHyl2',
    FALSE,
    'ACTIVE'
FROM demo_users
ON CONFLICT (username) DO UPDATE
SET
    email = EXCLUDED.email,
    display_name = EXCLUDED.display_name,
    password_hash = EXCLUDED.password_hash,
    must_change_password = FALSE,
    status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP;

WITH demo_users AS (
    SELECT *
    FROM (VALUES
        ('analyst01', 'ANALYST'),
        ('analyst02', 'ANALYST'),
        ('analyst03', 'ANALYST'),
        ('admin01', 'ADMINISTRATOR'),
        ('admin02', 'ADMINISTRATOR'),
        ('auditor01', 'AUDITOR'),
        ('root01', 'ROOT')
    ) AS t(username, role_code)
)
INSERT INTO user_roles (user_id, role_id)
SELECT users.id, roles.id
FROM demo_users
JOIN users ON users.username = demo_users.username
JOIN roles ON roles.code = demo_users.role_code
ON CONFLICT (user_id, role_id) DO NOTHING;

DELETE FROM processing_job_status_history
WHERE processing_job_id IN (
    SELECT id
    FROM processing_jobs
    WHERE id::text LIKE '50000000-0000-0000-0000-000000000%'
);

DELETE FROM processing_jobs
WHERE id::text LIKE '50000000-0000-0000-0000-000000000%';

DELETE FROM uploaded_files
WHERE id::text LIKE '60000000-0000-0000-0000-000000000%';

INSERT INTO uploaded_files (
    id,
    original_filename,
    stored_filename,
    storage_path,
    content_type,
    file_size,
    checksum,
    status,
    uploaded_by_user_id,
    uploaded_at,
    updated_at
) VALUES
    ('60000000-0000-0000-0000-000000000001', 'capital-requirements-jan.xlsx', 'capital-requirements-jan.xlsx', 'analyst01/capital-requirements-jan.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 125000, repeat('a', 64), 'STORED', '00000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP - INTERVAL '12 days', NULL),
    ('60000000-0000-0000-0000-000000000002', 'capital-requirements-feb.xlsx', 'capital-requirements-feb.xlsx', 'analyst01/capital-requirements-feb.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 130000, repeat('b', 64), 'STORED', '00000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP - INTERVAL '11 days', NULL),
    ('60000000-0000-0000-0000-000000000003', 'liquidity-risk-jan.xlsx', 'liquidity-risk-jan.xlsx', 'analyst02/liquidity-risk-jan.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 98000, repeat('c', 64), 'STORED', '00000000-0000-0000-0000-000000000011', CURRENT_TIMESTAMP - INTERVAL '10 days', NULL),
    ('60000000-0000-0000-0000-000000000004', 'liquidity-risk-feb.xlsx', 'liquidity-risk-feb.xlsx', 'analyst02/liquidity-risk-feb.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 99000, repeat('d', 64), 'STORED', '00000000-0000-0000-0000-000000000011', CURRENT_TIMESTAMP - INTERVAL '9 days', NULL),
    ('60000000-0000-0000-0000-000000000005', 'counterparty-exposure.xlsx', 'counterparty-exposure.xlsx', 'analyst03/counterparty-exposure.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 155000, repeat('e', 64), 'STORED', '00000000-0000-0000-0000-000000000012', CURRENT_TIMESTAMP - INTERVAL '8 days', NULL),
    ('60000000-0000-0000-0000-000000000006', 'operational-losses.xlsx', 'operational-losses.xlsx', 'analyst03/operational-losses.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 112000, repeat('f', 64), 'STORED', '00000000-0000-0000-0000-000000000012', CURRENT_TIMESTAMP - INTERVAL '7 days', NULL),
    ('60000000-0000-0000-0000-000000000007', 'credit-risk-q1.xlsx', 'credit-risk-q1.xlsx', 'analyst01/credit-risk-q1.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 143000, repeat('1', 64), 'STORED', '00000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP - INTERVAL '6 days', NULL),
    ('60000000-0000-0000-0000-000000000008', 'market-risk-q1.xlsx', 'market-risk-q1.xlsx', 'analyst02/market-risk-q1.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 121000, repeat('2', 64), 'STORED', '00000000-0000-0000-0000-000000000011', CURRENT_TIMESTAMP - INTERVAL '5 days', NULL),
    ('60000000-0000-0000-0000-000000000009', 'missing-source-file.xlsx', 'missing-source-file.xlsx', 'analyst01/missing-source-file.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 88000, repeat('3', 64), 'MISSING', '00000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '4 days'),
    ('60000000-0000-0000-0000-000000000010', 'failed-upload.xlsx', 'failed-upload.xlsx', 'analyst02/failed-upload.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 91000, repeat('4', 64), 'FAILED', '00000000-0000-0000-0000-000000000011', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days'),
    ('60000000-0000-0000-0000-000000000011', 'deleted-report.xlsx', 'deleted-report.xlsx', 'analyst03/deleted-report.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 76000, repeat('5', 64), 'DELETED', '00000000-0000-0000-0000-000000000012', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days');

INSERT INTO processing_jobs (
    id,
    uploaded_file_id,
    status,
    message,
    triggered_by_user_id,
    triggered_at,
    processing_completed_at,
    failure_reason,
    approved_by_user_id,
    approved_at,
    rejected_by_user_id,
    rejected_at,
    rejection_reason,
    revoked_by_user_id,
    revoked_at,
    revocation_reason,
    created_at,
    updated_at
) VALUES
    ('50000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000001', 'PENDING_EXECUTION', 'Waiting for execution', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '12 days', NULL),
    ('50000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000002', 'PROCESSING', 'Processing file rows', '00000000-0000-0000-0000-000000000002', CURRENT_TIMESTAMP - INTERVAL '10 days', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '11 days', CURRENT_TIMESTAMP - INTERVAL '10 days'),
    ('50000000-0000-0000-0000-000000000003', '60000000-0000-0000-0000-000000000003', 'PROCESSING_FAILED', 'Processing failed', '00000000-0000-0000-0000-000000000021', CURRENT_TIMESTAMP - INTERVAL '9 days', CURRENT_TIMESTAMP - INTERVAL '8 days 23 hours', 'Invalid worksheet layout', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '8 days 23 hours'),
    ('50000000-0000-0000-0000-000000000004', '60000000-0000-0000-0000-000000000004', 'AWAITING_APPROVAL', 'Processing completed; awaiting approval', '00000000-0000-0000-0000-000000000002', CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP - INTERVAL '7 days 22 hours', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '9 days', CURRENT_TIMESTAMP - INTERVAL '7 days 22 hours'),
    ('50000000-0000-0000-0000-000000000005', '60000000-0000-0000-0000-000000000005', 'APPROVED', 'Approved by admin01', '00000000-0000-0000-0000-000000000021', CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP - INTERVAL '6 days 22 hours', NULL, '00000000-0000-0000-0000-000000000002', CURRENT_TIMESTAMP - INTERVAL '6 days 20 hours', NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP - INTERVAL '6 days 20 hours'),
    ('50000000-0000-0000-0000-000000000006', '60000000-0000-0000-0000-000000000006', 'REJECTED', 'Rejected by admin02', '00000000-0000-0000-0000-000000000002', CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '5 days 22 hours', NULL, NULL, NULL, '00000000-0000-0000-0000-000000000021', CURRENT_TIMESTAMP - INTERVAL '5 days 20 hours', 'Totals do not match supporting evidence', NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP - INTERVAL '5 days 20 hours'),
    ('50000000-0000-0000-0000-000000000007', '60000000-0000-0000-0000-000000000007', 'REVOKED', 'Previously approved, later revoked', '00000000-0000-0000-0000-000000000021', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '4 days 22 hours', NULL, '00000000-0000-0000-0000-000000000002', CURRENT_TIMESTAMP - INTERVAL '4 days 20 hours', NULL, NULL, NULL, '00000000-0000-0000-0000-000000000021', CURRENT_TIMESTAMP - INTERVAL '4 days', 'New evidence invalidated approval', CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '4 days'),
    ('50000000-0000-0000-0000-000000000008', '60000000-0000-0000-0000-000000000008', 'APPROVED', 'Approved by admin02', '00000000-0000-0000-0000-000000000002', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '3 days 22 hours', NULL, '00000000-0000-0000-0000-000000000021', CURRENT_TIMESTAMP - INTERVAL '3 days 20 hours', NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '3 days 20 hours');

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
    'PENDING_EXECUTION',
    'USER',
    uploaded_file.uploaded_by_user_id,
    'File uploaded and queued for execution',
    job.created_at
FROM processing_jobs job
JOIN uploaded_files uploaded_file ON uploaded_file.id = job.uploaded_file_id
WHERE job.id::text LIKE '50000000-0000-0000-0000-000000000%';

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
    id,
    'PENDING_EXECUTION',
    'PROCESSING',
    'USER',
    triggered_by_user_id,
    'Administrator started processing',
    triggered_at
FROM processing_jobs
WHERE triggered_by_user_id IS NOT NULL
  AND id::text LIKE '50000000-0000-0000-0000-000000000%';

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
    id,
    'PROCESSING',
    'AWAITING_APPROVAL',
    'SYSTEM',
    NULL,
    'Automatic processing completed successfully',
    processing_completed_at
FROM processing_jobs
WHERE status IN ('AWAITING_APPROVAL', 'APPROVED', 'REJECTED', 'REVOKED')
  AND id::text LIKE '50000000-0000-0000-0000-000000000%';

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
    id,
    'PROCESSING',
    'PROCESSING_FAILED',
    'SYSTEM',
    NULL,
    failure_reason,
    processing_completed_at
FROM processing_jobs
WHERE status = 'PROCESSING_FAILED'
  AND id::text LIKE '50000000-0000-0000-0000-000000000%';

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
    id,
    'AWAITING_APPROVAL',
    'APPROVED',
    'USER',
    approved_by_user_id,
    'Administrator approved submission',
    approved_at
FROM processing_jobs
WHERE approved_by_user_id IS NOT NULL
  AND id::text LIKE '50000000-0000-0000-0000-000000000%';

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
    id,
    'AWAITING_APPROVAL',
    'REJECTED',
    'USER',
    rejected_by_user_id,
    rejection_reason,
    rejected_at
FROM processing_jobs
WHERE rejected_by_user_id IS NOT NULL
  AND id::text LIKE '50000000-0000-0000-0000-000000000%';

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
    id,
    'APPROVED',
    'REVOKED',
    'USER',
    revoked_by_user_id,
    revocation_reason,
    revoked_at
FROM processing_jobs
WHERE revoked_by_user_id IS NOT NULL
  AND id::text LIKE '50000000-0000-0000-0000-000000000%';

COMMIT;
