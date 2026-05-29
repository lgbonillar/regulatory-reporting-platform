# Database Model

Schema is managed by Flyway migrations under:

`backend/src/main/resources/db/migration`

## Core Principles

- UUID primary keys across domain tables.
- Workflow state columns stored as `VARCHAR` with application/domain validation.
- Histories and findings are persisted for auditability.
- One active auth session per user is enforced in database.

## Identity and Access Tables

### `users`

Key columns:

- `id` (PK)
- `username` (unique)
- `email` (unique)
- `display_name`
- `password_hash`
- `must_change_password`
- `status` (`ACTIVE|INACTIVE|LOCKED`)
- timestamps

### `roles`

Key columns:

- `id` (PK)
- `code` (unique) (`ANALYST`, `ADMINISTRATOR`, `AUDITOR`, `ROOT`)
- `name`

### `user_roles`

Join table:

- `user_id` (FK -> `users.id`)
- `role_id` (FK -> `roles.id`)
- composite PK (`user_id`, `role_id`)

### `auth_sessions`

Tracks refresh-token-backed sessions.

Key columns:

- `id` (PK)
- `user_id` (FK -> `users.id`)
- `refresh_token_hash`
- `user_agent`
- `ip_address`
- `created_at`
- `last_used_at`
- `expires_at`
- `revoked_at`
- `revoked_by_user_id` (FK -> `users.id`)
- `revoke_reason`

Important index:

- `uk_auth_sessions_active_user` unique partial index on `(user_id)` where `revoked_at IS NULL`

Invariant:

- A user can only have one active (non-revoked) session.

## Upload Domain Tables

### `uploaded_files`

Key columns:

- `id` (PK)
- `original_filename`
- `stored_filename`
- `storage_path`
- `content_type`
- `file_size`
- `checksum`
- `status` (`STORED|PENDING_CORRECTION|DELETED|MISSING|FAILED`)
- `uploaded_by_user_id` (FK -> `users.id`)
- timestamps

Important constraints/indexes:

- unique (`uploaded_by_user_id`, `original_filename`)
- indexes by owner and owner+status

### `uploaded_file_validation_runs`

Validation execution records.

Key columns:

- `id` (PK)
- `uploaded_file_id` (FK -> `uploaded_files.id`)
- `status` (`PASSED|FAILED|SYSTEM_FAILED`)
- `source` (e.g. `UPLOAD`, `REPLACEMENT`, `MANUAL`)
- `summary_message`
- `created_by`
- `created_at`

### `uploaded_file_findings`

Detailed findings linked to a validation run and file.

Key columns:

- `id` (PK)
- `validation_run_id` (FK -> `uploaded_file_validation_runs.id`)
- `uploaded_file_id` (FK -> `uploaded_files.id`)
- `severity`
- `scope`
- `code`
- `message`
- optional location/value fields (`sheet_name`, `row_number`, `column_name`, `field_name`, `expected_value`, `actual_value`)
- `created_at`

Indexes:

- by `uploaded_file_id`
- by `validation_run_id`
- by `severity`
- by `scope`

### `uploaded_file_status_history`

Audit trail for uploaded file status transitions.

Key columns:

- `id` (PK)
- `uploaded_file_id` (FK -> `uploaded_files.id`)
- `previous_status`
- `new_status`
- `transition_source`
- `transitioned_by_user_id` (FK -> `users.id`)
- `reason`
- `created_at`

## Processing Domain Tables

### `processing_jobs`

Key columns:

- `id` (PK)
- `uploaded_file_id` (FK -> `uploaded_files.id`, unique)
- `status` (`PENDING_EXECUTION|PROCESSING|PROCESSING_FAILED|AWAITING_APPROVAL|APPROVED|REJECTED|REVOKED`)
- `message`
- workflow actor/timestamp/reason columns for trigger, completion, approval, rejection, revocation
- `failure_reason`
- timestamps

Constraints:

- status check constraint
- one processing job per uploaded file (unique `uploaded_file_id`)

### `processing_job_status_history`

Processing workflow transition history.

Key columns:

- `id` (PK)
- `processing_job_id` (FK -> `processing_jobs.id`)
- `previous_status`
- `new_status`
- `transition_source` (`USER|SYSTEM`)
- `transitioned_by_user_id` (FK -> `users.id`)
- `reason`
- `created_at`

Important checks:

- status values for `previous_status` and `new_status`
- actor presence depending on `transition_source`

### `processing_job_findings`

Processing-stage findings.

Key columns:

- `id` (PK)
- `processing_job_id` (FK -> `processing_jobs.id`)
- `severity` (`ERROR|WARNING|INFO`)
- `scope` (`FILE_STRUCTURE|SHEET_STRUCTURE|COLUMN_STRUCTURE|ROW_DATA|BUSINESS_RULE|CROSS_FILE_VALIDATION|SYSTEM`)
- `code`
- `message`
- optional location/value fields
- `created_at`

## Key Relationships

- `users` 1..* `uploaded_files`
- `uploaded_files` 1..1 `processing_jobs` (current model)
- `uploaded_files` 1..* `uploaded_file_validation_runs`
- `uploaded_file_validation_runs` 1..* `uploaded_file_findings`
- `processing_jobs` 1..* `processing_job_status_history`
- `processing_jobs` 1..* `processing_job_findings`
- `users` 1..* `auth_sessions`

## Data Invariants to Preserve

- A processing start operation is valid only when uploaded file is processable (`STORED`).
- Validation findings must reference both run and file.
- History records should be appended, not mutated.
- Seed scripts must keep FK integrity and status consistency with business rules.

## Migration and Seed Discipline

- All schema changes require new Flyway migration files.
- Do not edit historical migrations in shared environments.
- Keep `database/dev` seeds aligned with current schema and endpoint expectations.
