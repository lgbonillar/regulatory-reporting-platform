# Architecture

## System Overview

Regulatory Reporting Platform is a monorepo with:

- `frontend/`: Angular SPA
- `backend/`: Spring Boot REST API
- `database/`: developer seed scripts
- `postman/`: API workflows and regression checks
- `samples/`: demo Excel files

Core business flow:

1. Analyst uploads or replaces an Excel file.
2. Backend stores metadata + binary and runs upload-time validation.
3. Validation runs and findings are persisted for traceability.
4. Processing jobs execute workflow transitions.
5. Administrators approve, reject, or revoke completed flows.
6. Histories and findings remain queryable for audit.

## Backend Architecture

Base package:

`dev.lgbonillar.regreporting`

Top-level backend modules:

- `config`
- `shared`
- `users`
- `upload`
- `processing`
- `modules`

### Module Responsibilities

`config`
- Security filter chain
- JWT encoder/decoder configuration
- CORS and OpenAPI configuration

`shared`
- API envelope (`ApiResponse`, `ApiErrorResponse`)
- Exception hierarchy
- Global exception mapping

`users`
- Login, refresh, logout
- Refresh token sessions (`auth_sessions`)
- Current user context and role checks

`upload`
- Uploaded file lifecycle
- Validation run creation and persistence
- Validation findings persistence
- Uploaded file status history
- File download + missing-file detection

`processing`
- Processing job lifecycle
- Processing findings
- Processing status history
- Approval/rejection/revocation workflow

`modules`
- Module-specific reusable logic
- Example: `modules/demo/validation` validators/layout rules reused across upload and processing

## Layering Pattern

Each feature should use this package structure when justified:

```text
feature/
├── controller
├── application
├── domain
├── infrastructure
└── dto
```

Intent:

- `controller`: HTTP boundary only
- `application`: orchestration and use-cases
- `domain`: invariants and state transitions
- `infrastructure`: JPA repositories/persistence adapters
- `dto`: request/response contracts

## Dependency Rules

- `upload` must not depend on `processing` application logic.
- `processing` must not depend on `upload` application services.
- Shared cross-module business logic belongs in `modules/<module>/...`.
- Cross-cutting framework concerns belong in `config` or `shared`.
- Controllers should not implement business rules.

## Security Architecture

Authentication model:

- Access token JWT (short TTL)
- Refresh token rotation backed by `auth_sessions`
- One active session per user enforced by unique partial index

Authorization model:

- Endpoint protection in Spring Security and `@PreAuthorize`
- Domain-level checks in services (ownership and workflow constraints)

Current high-level role access:

- `ANALYST`: uploads and starts own processing jobs
- `ADMINISTRATOR`: processing oversight + approval workflow
- `ROOT`: backend administrative authority where configured
- `AUDITOR`: currently restricted from report file endpoints

## Workflow Architecture

### Upload-time Validation First

Validation is executed when file is uploaded or replaced.

Results:

- Validation run persisted (`uploaded_file_validation_runs`)
- Findings persisted (`uploaded_file_findings`)
- Uploaded file status updated to:
  - `STORED` for pass
  - `PENDING_CORRECTION` for business/format validation failures
  - `FAILED` for technical/system failures

### Processing Preconditions

Processing can start only when:

- Job is in `PENDING_EXECUTION`
- Associated uploaded file is processable (`STORED`)

This prevents long-running processing on invalid or unavailable files.

## API Design

JSON endpoints use common envelope:

- `ApiResponse<T>` for success
- `ApiErrorResponse` for errors

Exception:

- Binary file download endpoint returns `Resource` directly:
  - `GET /api/report-files/{fileId}/download`

## Data Architecture

Primary entities:

- `users`, `roles`, `user_roles`, `auth_sessions`
- `uploaded_files`, `uploaded_file_status_history`
- `uploaded_file_validation_runs`, `uploaded_file_findings`
- `processing_jobs`, `processing_job_status_history`, `processing_job_findings`

All schema changes are managed through Flyway migrations.

## Extension Pattern for New Regulatory Modules

To add a new module (example `cnbv`):

1. Add upload validation implementation under `modules/cnbv/validation`.
2. Register validator in `UploadedFileValidatorRegistry`.
3. Add processing implementation under `processing/processor/cnbv`.
4. Register processor in `RegulatoryReportProcessorRegistry`.
5. Add tests + sample files + Postman scenarios.
6. Update docs (`business-rules`, `api-spec`, `database-model`) when behavior changes.
