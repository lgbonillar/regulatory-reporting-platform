# AGENTS.md

## Database Scope

This folder is for developer SQL seeds and database support scripts.

Source of truth for schema is Flyway migrations in:

`backend/src/main/resources/db/migration`

Do not treat seeds as schema definitions.

## Working Rules

- Apply schema changes only through new Flyway migrations.
- Do not rely on Hibernate auto-DDL for expected schema behavior.
- Keep seed scripts aligned with the latest migrated schema.
- Keep demo data realistic for current business workflows.

## Migration Rules

- Never modify an already-applied migration in shared environments.
- Use a new migration for every schema change.
- Keep migration names sequential and descriptive.
- Prefer explicit column names and predictable constraints/indexes.

## Seed Rules

- Seeds must be re-runnable on a clean database.
- Preserve referential integrity across all inserts.
- Keep role/user/session data consistent with auth rules.
- Keep workflow states consistent with backend invariants.

### Required Demo Coverage

Seeds should include scenarios for:

- Auth roles: `ANALYST`, `ADMINISTRATOR`, `AUDITOR`, `ROOT`
- Uploaded file states:
  - `STORED`
  - `PENDING_CORRECTION`
  - `FAILED`
  - `MISSING`
  - `DELETED`
- Validation run states:
  - `PASSED`
  - `FAILED`
  - `SYSTEM_FAILED`
- Findings for both validation and processing where applicable
- Status histories for auditability

### Processing Consistency

- A processing start flow should only be valid for processable files (`STORED`).
- Seed data may include legacy/conflict scenarios intentionally, but those should be clearly
labeled for negative testing.

## ID and Postman Compatibility

- Keep stable UUIDs for seeded entities referenced by Postman variables.
- If UUIDs change, update Postman collection/environment in the same change.
- Avoid random IDs in deterministic demo seeds unless there is a clear reason.

## Validation Checklist Before Finalizing

1. Run backend migrations on a clean DB.
2. Execute main seed script successfully.
3. Execute optional seed scripts only when required.
4. Verify key login/workflow scenarios with Postman.
5. Confirm seeded statuses and findings match current backend behavior.
6. Confirm no broken references remain in seed scripts.

## Security and Data Safety

- Never include real credentials or sensitive data.
- Use demo-safe values only.
- Keep passwords hashed when inserting users directly.
- Do not commit production dumps.

## Documentation Discipline

When changing workflow behavior, schema assumptions, or demo states:

- Update seed scripts
- Update Postman fixtures if IDs or scenarios changed
- Update backend/docs references when behavior changed
