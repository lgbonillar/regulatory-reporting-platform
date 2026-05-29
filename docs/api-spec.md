# API Specification

Base URL (local):

`http://localhost:8080`

## Authentication Model

- Access token: Bearer JWT
- Refresh token: opaque value sent in request body for refresh/logout operations
- Protected endpoints require:
  - `Authorization: Bearer <access_token>`

Public endpoints:

- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /actuator/health`
- `GET /swagger-ui/**`
- `GET /v3/api-docs/**`

## Response Envelope

JSON success responses use:

```json
{
  "success": true,
  "message": "Human-readable message",
  "data": {},
  "metadata": {
    "timestamp": "2026-05-29T00:00:00Z",
    "count": 1
  }
}
```

JSON error responses use:

```json
{
  "success": false,
  "message": "Business-safe error message",
  "error": {
    "code": "BUSINESS_CONFLICT",
    "details": null
  },
  "metadata": {
    "timestamp": "2026-05-29T00:00:00Z"
  }
}
```

Notes:

- `metadata.count` appears on list responses.
- File download endpoint returns binary content and does not use the JSON envelope.

## Auth Endpoints

### `POST /api/auth/login`

Request:

```json
{
  "username": "analyst01",
  "password": "password"
}
```

Success response data (`AuthResponse`):

```json
{
  "accessToken": "jwt",
  "refreshToken": "opaque-token",
  "tokenType": "Bearer",
  "expiresInSeconds": 900
}
```

Note: the JWT payload contains the following claims: `sub` (userId), `userId`, `username`, `role`, `sessionId`, `iss`, `iat`, `exp`. These are not part of the `data` envelope.

### `POST /api/auth/refresh`

Request:

```json
{
  "refreshToken": "opaque-token"
}
```

Returns a new access token and rotated refresh token.

### `POST /api/auth/logout`

Requires Bearer token.

Request:

```json
{
  "refreshToken": "opaque-token"
}
```

Response: `204 No Content`.

## Report Files Endpoints

### `GET /api/report-files?username={username}`

Roles:

- `ANALYST`, `ADMINISTRATOR`, `ROOT`

Returns uploaded files visible to the caller.

### `POST /api/report-files` (multipart/form-data)

Roles:

- `ANALYST`

Form fields:

- `file`: Excel file

Behavior:

- Stores file
- Executes upload validation
- Persists validation run/findings
- Returns uploaded file state and optional processing job details

### `PUT /api/report-files/{fileId}` (multipart/form-data)

Roles:

- `ANALYST`

Replaces file content and re-runs validation pipeline.

### `DELETE /api/report-files/{fileId}`

Roles:

- `ANALYST`

Response: `204 No Content`.

### `GET /api/report-files/{fileId}/download`

Roles:

- `ANALYST`, `ADMINISTRATOR`, `ROOT`

Returns binary file stream.

### `GET /api/report-files/{fileId}/validation-runs`

Roles:

- `ANALYST`, `ADMINISTRATOR`, `ROOT`

Response list item shape (`UploadedFileValidationRunResponse`):

```json
{
  "id": "uuid",
  "uploadedFileId": "uuid",
  "status": "PASSED",
  "source": "UPLOAD",
  "summaryMessage": "Validation passed",
  "createdBy": "analyst01",
  "createdAt": "2026-05-29T00:00:00Z"
}
```

### `GET /api/report-files/{fileId}/findings`

Roles:

- `ANALYST`, `ADMINISTRATOR`, `ROOT`

Returns findings across all validation runs for the file.

### `GET /api/report-files/{fileId}/validation-runs/{validationRunId}/findings`

Roles:

- `ANALYST`, `ADMINISTRATOR`, `ROOT`

Returns findings for one validation run.

Finding item shape (`UploadedFileFindingResponse`):

```json
{
  "id": "uuid",
  "validationRunId": "uuid",
  "uploadedFileId": "uuid",
  "severity": "ERROR",
  "scope": "COLUMN_STRUCTURE",
  "code": "REQUIRED_COLUMN_MISSING",
  "message": "Column 'amount' is required",
  "sheetName": "sales",
  "rowNumber": null,
  "columnName": "amount",
  "fieldName": "amount",
  "rejectedValue": null,
  "expectedValue": "numeric column",
  "actualValue": "missing",
  "createdAt": "2026-05-29T00:00:00Z"
}
```

## Processing Jobs Endpoints

### `GET /api/processing-jobs?username={optional}`

Roles:

- `ANALYST`, `ADMINISTRATOR`

Analyst sees own jobs only.

### `GET /api/processing-jobs/{jobId}`

Roles:

- `ANALYST`, `ADMINISTRATOR`

### `POST /api/processing-jobs/{jobId}/start`

Roles:

- `ANALYST`

Preconditions:

- Job must be `PENDING_EXECUTION`
- File must be processable (`STORED`)

### `POST /api/processing-jobs/{jobId}/complete`

Roles:

- `ADMINISTRATOR`

### `POST /api/processing-jobs/{jobId}/fail`

Roles:

- `ADMINISTRATOR`

Request:

```json
{
  "reason": "Technical reason"
}
```

### `POST /api/processing-jobs/{jobId}/approve`

Roles:

- `ADMINISTRATOR`

### `POST /api/processing-jobs/{jobId}/reject`

Roles:

- `ADMINISTRATOR`

Request:

```json
{
  "reason": "Business rejection reason"
}
```

### `POST /api/processing-jobs/{jobId}/revoke`

Roles:

- `ADMINISTRATOR`

Request:

```json
{
  "reason": "Revocation reason"
}
```

### `GET /api/processing-jobs/{jobId}/history`

Roles:

- `ANALYST`, `ADMINISTRATOR`

Returns processing status history.

### `GET /api/processing-jobs/{jobId}/findings`

Roles:

- `ANALYST`, `ADMINISTRATOR`

Returns processing findings.

## Status and Error Expectations

Common statuses:

- `200 OK`
- `204 No Content`
- `400 Bad Request`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`
- `409 Conflict`

Typical error codes (from `ApiErrorResponse.error.code`):

- `BAD_REQUEST`
- `UNAUTHORIZED`
- `FORBIDDEN`
- `NOT_FOUND`
- `BUSINESS_CONFLICT`
- `INTERNAL_ERROR`

## OpenAPI

Interactive docs:

- `/swagger-ui/index.html`

OpenAPI JSON:

- `/v3/api-docs`
