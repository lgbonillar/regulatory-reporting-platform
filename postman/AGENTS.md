# AGENTS.md

## Scope

This folder contains Postman collection and environment artifacts used to validate backend behavior and demo flows.

## Working Rules

- Keep collection requests aligned with current backend endpoints and payload contracts.
- Keep environment variables aligned with seeded IDs and token flows.
- When backend status rules change, update request tests accordingly.

## Required Alignment

When changing backend behavior for auth, upload, validation, or processing:

- Update collection requests if endpoint path or body changed.
- Update tests/assertions for response status, response shape, and business status values.
- Update environment variables for any changed seeded UUIDs.

## Auth Rules

- Login, refresh, and logout requests must remain valid for current JWT/session behavior.
- Do not hardcode production credentials.
- Use demo-only credentials aligned with `database/dev` seeds.

## Workflow Coverage

Collection should cover at least:

- Success login + token refresh
- Uploaded file list/upload/detail flows
- Validation runs and findings queries
- Processing list/detail/workflow actions
- Role-based forbidden scenarios
- Conflict scenarios for invalid state transitions

## Validation Checklist

Before considering Postman updates complete:

1. Import collection and environment successfully.
2. Run auth requests and confirm token variable updates.
3. Execute core upload and processing scenarios.
4. Confirm negative role/state tests still fail with expected status codes.
5. Ensure no stale environment variables remain.
