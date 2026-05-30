# Business Rules

## Roles

System roles:

- `ANALYST`
- `ADMINISTRATOR`
- `AUDITOR`
- `ROOT`

Current behavior:

- Analysts upload/replace/delete their own files and start processing on their jobs. Analysts can list files for their own username only; listing another analyst's files returns 403 Forbidden.
- Administrators can list files for any username.
- Administrators manage processing workflow decisions (complete/fail/approve/reject/revoke). Administrators can list processing jobs for any username; analysts can list jobs for their own username only; listing another analyst's jobs returns 403 Forbidden.
- Root has high-level administrative authority where configured by backend rules.
- Auditors currently do not have access to report file endpoints.

## Uploaded File Rules

Uploaded file status values:

- `STORED`
- `PENDING_CORRECTION`
- `DELETED`
- `MISSING`
- `FAILED`

### Meaning

`STORED`
- File binary and metadata are available and validation passed.

`PENDING_CORRECTION`
- File failed validation checks and needs replacement/correction.

`DELETED`
- File was logically deleted and should not continue workflow operations.

`MISSING`
- Metadata exists but physical file is not available in storage.

`FAILED`
- Technical/system failure occurred during validation or storage pipeline.

### Validation-on-Upload Rule

When an analyst uploads or replaces a file:

1. File is stored.
2. Validator runs.
3. Validation run is persisted.
4. Findings are persisted.
5. File status is updated based on result.

No processing step should be required to discover structural/business validation issues that can be detected at upload time.

### Processing Eligibility Rule

A file can be processed only when it is processable, currently:

- `STORED`

If file status is `PENDING_CORRECTION`, `FAILED`, `MISSING`, or `DELETED`, processing start must fail with conflict.

## Upload Validation Rules

Validation output persists:

- `uploaded_file_validation_runs`
- `uploaded_file_findings`

Validation run statuses:

- `PASSED`
- `FAILED`
- `SYSTEM_FAILED`

Validation findings should include enough context for remediation:

- severity
- scope
- code
- message
- optional sheet/row/column/field and expected vs actual values

## Processing Job Rules

Processing job statuses:

- `PENDING_EXECUTION`
- `PROCESSING`
- `PROCESSING_FAILED`
- `AWAITING_APPROVAL`
- `APPROVED`
- `REJECTED`
- `REVOKED`

### Workflow intent

`PENDING_EXECUTION`
- Job was created and is waiting to start.

`PROCESSING`
- Job is currently running.

`PROCESSING_FAILED`
- Job failed technically or in non-recoverable processing checks.

`AWAITING_APPROVAL`
- Processing completed technically and requires admin decision.

`APPROVED`
- Admin approved final result.

`REJECTED`
- Admin rejected final result.

`REVOKED`
- Prior approval was later invalidated.

### Transition rules

Allowed transitions are enforced by domain/service logic. Core expected transitions:

- `PENDING_EXECUTION` -> `PROCESSING`
- `PROCESSING` -> `AWAITING_APPROVAL`
- `PROCESSING` -> `PROCESSING_FAILED`
- `AWAITING_APPROVAL` -> `APPROVED`
- `AWAITING_APPROVAL` -> `REJECTED`
- `APPROVED` -> `REVOKED`
- `PENDING_EXECUTION` -> `REVOKED`

Status histories are persisted for uploaded files and processing jobs.

## Session and Token Rules

- Login creates or rotates active session.
- Refresh rotates refresh token and reissues access token.
- Logout revokes session by refresh token and current user.
- Active session validation is enforced in JWT decoding path.
- One active session per user is enforced at database level.

## Error Handling Rules

- API uses structured error envelope.
- Forbidden access must return authorization errors, not silent empty data.
- Invalid state transitions must return conflict errors.
- Not-found entities return explicit not-found errors.

## Auditability Rules

Persist and expose:

- Uploaded file status history
- Processing job status history
- Validation runs and findings
- Processing findings

Audit data is read-only from client perspective.
