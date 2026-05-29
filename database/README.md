# Database

Database assets for the Regulatory Reporting Platform.

This folder contains developer-focused SQL scripts used to seed demo data after running backend
migrations.

## Purpose

Use this area to:

- Load demo users, roles, sessions, files, processing jobs, and findings
- Recreate realistic workflow states for local testing
- Keep Postman and frontend demos aligned with backend behavior

Schema definition is managed by Flyway migrations in:

`backend/src/main/resources/db/migration`

## Folder Structure

```text
database/
└── dev/
    ├── seed_auth_workflow_demo.sql
    └── seed_processing_jobs_demo.sql
```

## Prerequisites

- PostgreSQL running locally
- Backend migrations already applied

## Reset and Seed a Clean Local Database

Example using a local database named regulatory_reporting:

```
dropdb regulatory_reporting
createdb regulatory_reporting
cd backend
./mvnw flyway:migrate
cd ..
psql -d regulatory_reporting -f database/dev/seed_auth_workflow_demo.sql
```

Optional extra seed (only if needed for additional scenarios):

`psql -d regulatory_reporting -f database/dev/seed_processing_jobs_demo.sql`

## Recommended Seed Order

1. Run Flyway migrations from backend
2. Run seed_auth_workflow_demo.sql
3. Run seed_processing_jobs_demo.sql only if the scenario requires it

## What the Main Seed Covers

`seed_auth_workflow_demo.sql` is expected to include:

- Roles: ANALYST, ADMINISTRATOR, AUDITOR, ROOT
- Users with demo credentials
- Uploaded files across states:
    - STORED
    - PENDING_CORRECTION
    - FAILED
    - MISSING
    - DELETED

- Validation runs:
    - PASSED
    - FAILED
    - SYSTEM_FAILED

- Uploaded file findings and status history
- Processing jobs and workflow scenarios aligned with current backend rules

## Demo Credentials

If your seed keeps the current convention, users are usually configured with password:

password

Always confirm in the seed before sharing demos.

## Consistency Rules

- Seeds must match the current Flyway schema.
- Seeds must respect foreign keys and valid status transitions.
- When backend workflow rules change, update seeds in the same change.
- Keep IDs stable when Postman collections depend on fixed UUIDs.

## Troubleshooting

### Flyway checksum mismatch

Do not edit applied migration files retroactively in shared flows.
Create a new migration for schema changes.
For local-only reset, recreate the database and rerun migrations.

### Duplicate key or FK violations when seeding

You likely seeded into a non-clean database or ran scripts in a wrong order.
Reset database, rerun migrations, then rerun seeds in recommended order.

### Login works but workflow data looks inconsistent

Check that:

1. Migrations are up to date
2. Main seed executed successfully
3. Postman environment points to current seeded IDs

