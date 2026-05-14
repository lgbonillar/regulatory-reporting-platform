# AGENTS.md

## Backend Context

This backend is a Spring Boot REST API for a private enterprise regulatory reporting platform.

The API is responsible for:

- Receiving Excel file uploads
- Validating file layouts
- Applying financial and regulatory business rules
- Persisting processing jobs
- Storing validation errors
- Generating downloadable reports
- Exposing audit logs
- Supporting role-based access control

## Technology Stack

- Java 21
- Spring Boot
- Maven Wrapper
- Spring Web
- Spring Validation
- Spring Data JPA
- PostgreSQL
- Flyway
- Lombok
- Actuator
- Testcontainers when integration tests are needed

## Project Structure

Use the following package structure under the base package:

```text
com.mrcrafterman.regreporting
├── config
├── security
├── shared
├── auth
├── users
├── upload
├── processing
├── reports
└── audit
```

### `config`

Use for framework and application configuration.

Examples:

- CORS configuration
- OpenAPI configuration
- Jackson configuration
- Storage configuration
- Async configuration

### `security`

Use for authentication and authorization configuration.

Examples:

- Security filter chain
- JWT configuration
- Role/authority mapping
- Security utilities

### `shared`

Use for cross-cutting code.

Examples:

- Global exception handling
- Common response models
- Error codes
- Shared validation utilities
- Date/time utilities

Do not place feature-specific business logic in `shared`.

### Feature Packages

Each feature package should be organized by responsibility when needed:

```text
feature/
├── controller
├── application
├── domain
├── infrastructure
└── dto
```

Use this structure when the feature grows enough to justify it. Do not create empty folders without a reason.

## Architecture Guidelines

- Keep controllers thin.
- Put business use cases in application services.
- Keep persistence details in infrastructure/repositories.
- Keep domain rules close to the domain model or use case that owns them.
- Do not place business logic directly in controllers.
- Do not expose JPA entities directly through REST APIs.
- Use DTOs for API requests and responses.
- Validate input using Bean Validation annotations where applicable.
- Prefer constructor injection.
- Avoid field injection.

## REST API Guidelines

- Use clear, resource-oriented endpoints.
- Use plural resource names.
- Return meaningful HTTP status codes.
- Use typed request and response DTOs.
- Keep error responses consistent.
- Do not expose stack traces or internal exception details in API responses.

Example endpoint style:

```text
POST   /api/report-files
GET    /api/processing-jobs
GET    /api/processing-jobs/{id}
GET    /api/processing-jobs/{id}/validation-errors
GET    /api/reports
GET    /api/reports/{id}/download
GET    /api/audit-logs
```

## Database Guidelines

- Use Flyway migrations for schema changes.
- Do not rely on Hibernate auto-DDL for production-like schema creation.
- Keep migration names clear and sequential.

Example:

```text
V1__create_initial_schema.sql
V2__create_processing_jobs.sql
V3__create_validation_errors.sql
```

- Prefer explicit column names.
- Use indexes for frequently searched fields.
- Use audit fields where useful, such as `created_at`, `created_by`, `updated_at`, and `updated_by`.

## JPA Guidelines

- Do not expose entities as API responses.
- Prefer `Long` or `UUID` identifiers depending on the entity.
- Keep relationships simple.
- Avoid unnecessary bidirectional relationships.
- Be careful with lazy loading in API responses.
- Use repositories only for persistence access, not business orchestration.

## Excel Processing Guidelines

- Validate file extension and content type, but do not trust them as the only validation.
- Validate layout before processing business rows.
- Return clear validation errors with row, column, field, message, and severity when possible.
- Keep parsing, layout validation, business validation, and persistence separated.
- Do not process large files directly inside controllers.
- Avoid loading unnecessary data into memory when a streaming approach is reasonable.

## Error Handling

Use a global exception handler for API errors.

Prefer consistent responses with fields like:

```json
{
  "timestamp": "2026-01-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid report layout",
  "path": "/api/report-files"
}
```

Do not return raw Java exception messages to the frontend if they expose internal details.

## Testing Guidelines

- Add unit tests for business rules.
- Add tests for validation services.
- Add controller tests for important API flows.
- Use integration tests when database behavior matters.
- Use Testcontainers for PostgreSQL integration tests when applicable.
- Prefer meaningful tests over superficial coverage.

Before considering backend work complete, run:

```bash
./mvnw test
```

If modifying application startup, configuration, migrations, or dependencies, also verify the application starts:

```bash
./mvnw spring-boot:run
```

## Code Style

- Use clear class and method names.
- Avoid overly generic names like `Manager`, `Helper`, or `Util` unless truly justified.
- Keep methods small and readable.
- Avoid duplicated validation logic.
- Prefer immutable DTOs when practical.
- Avoid `null` when an empty collection or `Optional` is clearer.
- Do not add comments that only repeat what the code says.
- Add comments only for non-obvious business rules or technical decisions.

## Security Guidelines

- Never hardcode secrets.
- Never commit real credentials.
- Do not disable security globally unless explicitly requested for local development.
- Backend authorization is mandatory; frontend guards are only UX.
- Validate all uploaded files server-side.
- Do not trust client-provided roles, user IDs, or permissions.