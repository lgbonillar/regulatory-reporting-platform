# AGENTS.md

## Backend Context

This backend is a Spring Boot REST API for the Regulatory Reporting Platform.

Primary responsibilities:

- Authentication and session lifecycle (JWT + refresh token)
- Uploaded file management
- File validation runs and findings
- Processing job workflow and findings
- Role-based authorization and audit-oriented status history

## Technology Stack

- Java 21
- Spring Boot
- Maven Wrapper
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Lombok
- springdoc-openapi
- Testcontainers (when integration tests are needed)

## Runtime Baseline

- Use Java 21 for all local backend commands.
- Prefer `./mvnw` over a global Maven install to keep builds reproducible.
- If local Java is switched with SDKMAN or shell profile, verify `java -version` before running backend commands.

## Package Structure

Base package:

```text
dev.lgbonillar.regreporting
```

Top-level modules currently used:

```text
config
shared
users
upload
processing
modules
```

Feature packages should follow this structure when justified:

```text
feature/
├── controller
├── application
├── domain
├── infrastructure
└── dto
```

Do not create empty folders only for style.

## Current Domain Boundaries

- `users`: auth, sessions, roles, identity context.
- `upload`: uploaded file lifecycle, file validation runs, upload findings, upload status history.
- `processing`: processing job lifecycle, processing findings, processing status history.
- `modules`: module-specific validation/processing assets (for example `modules/demo/validation`) intended for reuse across upload and processing flows.
- `shared`: cross-cutting API response envelope, exception hierarchy, global exception handler.
- `config`: security config, JWT properties, OpenAPI config, CORS, and related wiring.

Do not move logic from one bounded area to another unless the change explicitly requires it.

## Architecture Guidelines

- Keep controllers thin; orchestrate use cases in `application`.
- Keep domain invariants in `domain` entities/services.
- Keep persistence concerns in `infrastructure`.
- Do not expose entities directly in REST responses.
- Use DTOs for request/response boundaries.
- Prefer constructor injection.
- Avoid field injection.
- Avoid introducing abstractions without repeated use or clear complexity reduction.

## API Guidelines

- Keep API responses consistent with `ApiResponse` and `ApiErrorResponse`.
- Use meaningful HTTP status codes and business-safe error messages.
- Do not leak stack traces or internal implementation details.
- Keep endpoint naming resource-oriented and plural when possible.

Current core endpoint groups:

- `/api/auth/*`
- `/api/report-files/*` including validation runs and findings endpoints
- `/api/processing-jobs/*`

## Validation and Processing Rules

- Uploaded files must be validated during upload/replacement flow.
- Persist validation runs and findings for traceability.
- Processing must only start for processable file states.
- Keep generic Excel rule helpers reusable; keep module-specific business rules under `modules/<module>`.

## Database Guidelines

- All schema changes must go through Flyway migrations.
- Migration files must stay sequential and descriptive.
- Do not use ad hoc schema changes outside migrations.
- Keep seed scripts aligned with current domain states and workflow rules.

## Testing Guidelines

- Add or update unit tests for domain/application rule changes.
- Add controller tests when API behavior or auth rules change.
- Add integration tests when persistence behavior is material.
- Prefer targeted suites during active iteration; run full suite before finalizing.

Baseline command:

```bash
./mvnw test
```

For startup/config/migration/security changes, also verify boot:

```bash
./mvnw spring-boot:run
```

## Security Guidelines

- Never commit secrets, private keys, or production credentials.
- Do not disable backend authorization to bypass role checks.
- Do not trust client-provided role or user identifiers.
- Keep JWT and session checks enforced at endpoint/security layer.

## Documentation Discipline

When behavior changes, align docs in the same change when practical:

- Root `README.md` for high-level context/run instructions.
- `docs/` for architecture, API, and business-rule updates.
- Postman collection and seeds when API or flow behavior changes.
