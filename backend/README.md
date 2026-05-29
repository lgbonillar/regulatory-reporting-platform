# Backend

Spring Boot REST API for the Regulatory Reporting Platform.

This service handles:

- Authentication and session management (JWT + refresh token)
- Uploaded file lifecycle
- Validation runs and validation findings
- Processing jobs and processing findings
- Role-based authorization

## Tech Stack

- Java 21
- Spring Boot
- Maven Wrapper
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Lombok
- springdoc-openapi

## Prerequisites

- JDK 21
- Maven (or use `./mvnw`)
- PostgreSQL

## Local Run

From `backend/`:

```bash
./mvnw spring-boot:run
```

Default local URL:

- `http://localhost:8080`

## Tests

Run all tests:

```bash
./mvnw test
```

Run a targeted suite:

```bash
./mvnw test -Dtest=ProcessingJobWorkflowServiceTest
```

## Database and Migrations

Flyway migrations are located at:

`src/main/resources/db/migration`

Apply migrations via app startup or Maven Flyway command:

```bash
./mvnw flyway:migrate
```

Developer seeds live in root `database/dev/` and should remain aligned with current workflow states.

## API Documentation

OpenAPI/Swagger is enabled by `OpenApiConfig`.

Common local URLs:

- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/v3/api-docs`

## Package Overview

Base package:

```text
dev.lgbonillar.regreporting
```

Current top-level areas:

```text
config
shared
users
upload
processing
modules
```

High-level intent:

- `config`: application and security configuration
- `shared`: API envelope and cross-cutting error handling
- `users`: auth/session/role domain and endpoints
- `upload`: file upload, status lifecycle, validation runs/findings
- `processing`: processing workflow and findings
- `modules`: module-specific business validation/processing assets

## Main Endpoint Groups

- `/api/auth/*`
- `/api/report-files/*`
- `/api/processing-jobs/*`

## Development Rules

- Keep controllers thin; business orchestration belongs in `application`.
- Keep domain invariants in `domain`.
- Keep persistence logic in `infrastructure`.
- Do not expose entities directly in API responses.
- Use DTOs for request/response contracts.
- Keep tests and seeds aligned with behavioral changes.

## Related Docs

- [Root README](../README.md)
- [Backend AGENTS](./AGENTS.md)
- [Architecture](../docs/architecture.md)
- [API Spec](../docs/api-spec.md)
- [Business Rules](../docs/business-rules.md)
- [Database Model](../docs/database-model.md)
