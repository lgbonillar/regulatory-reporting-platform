# Deployment and Runtime

This document covers local runtime baseline and environment configuration for backend/frontend execution.

## Runtime Baseline

- Backend: Java 21
- Backend build tool: Maven Wrapper (`./mvnw`)
- Database: PostgreSQL
- Frontend: Node.js (pinned with `.nvmrc` in `frontend/`)

## Backend Environment Variables

Backend reads config from `backend/src/main/resources/application.yml`.

Main variables:

- `DB_URL`  
  Default: `jdbc:postgresql://localhost:5432/regulatory_reporting`
- `DB_USERNAME`  
  Default: `regreporting`
- `DB_PASSWORD`  
  Default: `regreporting`

- `APP_STORAGE_UPLOAD_DIR`  
  Default: `storage/uploads`
- `APP_JWT_SECRET`  
  Default: `dev-only-change-this-secret-dev-only-change-this-secret`

Static values in current config:

- JWT issuer: `regulatory-reporting-api`
- Access token expiration: `15` minutes
- Refresh token expiration: `7` days
- Server port: `8080`

## Local Backend Startup

From `backend/`:

```bash
./mvnw spring-boot:run
```

Health endpoint:

`http://localhost:8080/actuator/health`

Swagger UI:

`http://localhost:8080/swagger-ui/index.html`

OpenAPI JSON:

`http://localhost:8080/v3/api-docs`

## Database Provisioning (Local)

Example flow:

```bash
dropdb regulatory_reporting
createdb regulatory_reporting
cd backend
./mvnw flyway:migrate
cd ..
psql -d regulatory_reporting -f database/dev/seed_auth_workflow_demo.sql
```

Optional additional seed:

```bash
psql -d regulatory_reporting -f database/dev/seed_processing_jobs_demo.sql
```

## Frontend Startup

From `frontend/`:

```bash
nvm use
npm install
npm start
```

Frontend URL:

`http://localhost:4200`

## Postman Runtime Notes

Use:

- `postman/regulatory-reporting-platform.postman_collection.json`
- `postman/regulatory-reporting-platform.local.postman_environment.json`

Flow:

1. Login
2. Upload/list files
3. Validation runs/findings
4. Processing transitions
5. Role/state negative checks

## Security Notes

- Do not use default JWT secret outside local development.
- Do not commit real credentials.
- Keep production secrets in environment/secret manager, not in repository.

## Deployment Checklist (Any Environment)

1. Confirm Java 21 runtime.
2. Set environment-specific DB and JWT values.
3. Run migrations before serving traffic.
4. Verify `/actuator/health`.
5. Verify auth + upload + processing smoke tests.
6. Verify storage path write permissions.
7. Verify role-based access behavior for critical endpoints.
