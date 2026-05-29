# Postman

Postman assets for backend API testing and demo workflows.

## Files

- `regulatory-reporting-platform.postman_collection.json`
- `regulatory-reporting-platform.local.postman_environment.json`

## Import Order

1. Import the collection file.
2. Import the local environment file.
3. Select the imported environment before sending requests.

## Required Environment Variables

Typical variables expected by the collection:

- `baseUrl`
- `accessToken`
- `refreshToken`
- seeded IDs for files, validation runs, and jobs used in demo scenarios

## Recommended Execution Flow

1. Login and store tokens (`/api/auth/login`).
2. Refresh token flow (`/api/auth/refresh`) when needed.
3. Run uploaded file flows:
   - list files
   - upload valid/invalid files
   - query validation runs/findings
4. Run processing flows:
   - list jobs
   - start processing
   - approve/reject/revoke where allowed
5. Run role-based negative checks:
   - unauthorized role access
   - conflict scenarios (for example non-processable file states)

## Seed Alignment

Collection scenarios assume demo data from `database/dev/` is loaded and aligned with current backend schema and workflow rules.

If seeded UUIDs or status flows change, update this collection and environment in the same change.

## Troubleshooting

### `401 Unauthorized`

- Ensure login request succeeded.
- Ensure `accessToken` variable is present and current.
- Retry refresh flow if token expired.

### `403 Forbidden`

- Validate user role for the requested endpoint.
- Some scenarios are intentionally role-restricted.

### `409 Conflict`

- Confirm the operation is valid for current domain state.
- Example: processing a job whose uploaded file is not processable.
