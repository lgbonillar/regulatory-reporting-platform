# Testing Strategy

## Goals

- Protect domain invariants and workflow transitions.
- Prevent regressions in role-based access control.
- Validate API contracts and error semantics.
- Keep demo seeds and Postman scenarios aligned with real behavior.

## Test Layers

### 1) Domain and Application Unit Tests

Scope:

- Status transitions and invariants
- Validation orchestration
- Permission checks in service layer

Examples:

- Uploaded file processability guard
- Processing workflow transition constraints
- Validation run/finding persistence logic

### 2) Controller Tests

Scope:

- Endpoint authorization
- Request validation
- Response envelope shape
- Expected HTTP codes (`200`, `204`, `400`, `403`, `404`, `409`)

### 3) Repository/Integration Tests

Scope:

- Persistence behavior that unit tests cannot prove
- Constraint assumptions
- Query filters by owner/role

Use Testcontainers when database-real behavior matters.

### 4) API Workflow Tests (Postman)

Scope:

- Login/refresh/logout flow
- Upload valid/invalid file flow
- Validation runs/findings retrieval
- Processing actions and negative state checks

## Required Regression Scenarios

Minimum set before release/demo:

1. Successful login and refresh rotation.
2. Upload valid file -> status `STORED`.
3. Upload invalid file -> status `PENDING_CORRECTION` with findings.
4. Attempt to start processing for non-processable file -> `409`.
5. Processing success path to `AWAITING_APPROVAL`.
6. Admin approve/reject/revoke transitions.
7. Analyst cannot access other analyst data.
8. Auditor restrictions for report-file endpoints.

## Commands

Backend full suite:

```bash
cd backend
./mvnw test
```

Targeted suite example:

```bash
cd backend
./mvnw test -Dtest=ProcessingJobWorkflowServiceTest
```

## Data and Fixture Discipline

- Keep seeds deterministic for reproducible workflow tests.
- Keep UUID references stable when Postman depends on them.
- Update Postman and seeds in the same change when endpoint behavior changes.

## Documentation Discipline

When changing behavior:

- Update tests
- Update Postman fixtures
- Update docs (`business-rules`, `api-spec`, `database-model`) in the same change when practical
