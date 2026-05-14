# Regulatory Reporting Platform 📊

Full-stack platform for financial Excel validation, regulatory report processing, audit tracking, and enterprise-ready reporting.

## Overview 🚀

**Regulatory Reporting Platform** is a portfolio-grade enterprise application designed to simulate a real-world financial reporting workflow.

The platform focuses on uploading Excel files, validating financial data, applying business rules, tracking processing jobs, auditing user actions, and generating downloadable regulatory reports.

This project is built to demonstrate senior full-stack engineering skills through a realistic business domain involving:

- Enterprise application architecture
- Financial data processing
- Excel layout validation
- Business rule execution
- Processing history
- Auditability
- Backend persistence
- Frontend dashboards
- Docker-based local setup
- Maintainable documentation

## Project Status 🧱

This project is currently in its **initial MVP setup phase**.

The current goal is to build a solid foundation before adding advanced features like authentication, role-based access control, report exports, background jobs, and Keycloak integration.

## Main Features ✨

Planned and in-progress features include:

- 📤 Excel file upload
- ✅ File layout validation
- 🧮 Financial business rule validation
- 📊 Processing dashboard
- 📝 Validation error tracking
- 🗂️ Processing job history
- 📥 Downloadable reports
- 🔎 Audit logs
- 👥 Role-based access control
- 🐳 Docker Compose local environment
- 🧪 Unit and integration testing

## Tech Stack 🛠️

### Frontend

- Angular
- TypeScript
- Client-side rendered SPA
- Standalone components
- Signals for local UI state
- Reactive Forms
- Lazy-loaded feature routes
- Accessibility-conscious UI

### Backend

- Java 21
- Spring Boot
- Maven Wrapper
- Spring Web
- Spring Validation
- Spring Data JPA
- PostgreSQL Driver
- Flyway Migration
- Lombok
- Spring Boot Actuator
- Spring Boot DevTools
- Testcontainers

### Database & Infrastructure

- PostgreSQL
- Flyway database migrations
- Docker Compose

## Repository Structure 📁

```text
regulatory-reporting-platform/
├── frontend/
├── backend/
├── database/
├── docs/
├── samples/
├── postman/
├── docker-compose.yml
├── AGENTS.md
├── PROJECT_CONTEXT.md
├── PLAN.md
└── README.md
```

## Frontend Architecture 🎨

The Angular application is organized by responsibility and business features:

```text
frontend/src/app/
├── core/
├── layout/
├── shared/
└── features/
```

### Main frontend areas

- `core/` — authentication, guards, interceptors, configuration, global services
- `layout/` — shell, sidebar, topbar, footer, navigation layout
- `shared/` — reusable components, pipes, directives, utilities, generic models
- `features/` — business features grouped by domain

Planned feature modules:

```text
features/
├── auth/
├── dashboard/
├── file-upload/
├── processing/
├── reports/
├── audit/
└── users/
```

## Backend Architecture ☕

The backend is a Spring Boot REST API organized around business domains.

Base package:

```text
com.mrcrafterman.regreporting
```

Recommended backend package structure:

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

Feature packages may be organized as:

```text
feature/
├── controller
├── application
├── domain
├── infrastructure
└── dto
```

The backend follows these principles:

- Thin controllers
- DTOs for API requests and responses
- Business logic inside application/domain services
- Persistence handled through repositories
- Flyway-managed database schema
- Consistent error handling
- Server-side validation for uploaded files

## Rendering Strategy 🧠

The frontend is designed as a **client-side rendered Angular SPA**.

SSR and SSG are intentionally not part of the MVP because this is a private, authenticated enterprise application focused on dynamic data, dashboards, file uploads, and user-specific workflows.

## Getting Started ⚡

### Prerequisites

Make sure you have installed:

- Node.js
- npm
- Java 21
- Docker
- Docker Compose

## Running the Project Locally 🧑‍💻

### 1. Clone the repository

```bash
git clone <repository-url>
cd regulatory-reporting-platform
```

### 2. Start the database

```bash
docker compose up -d
```

### 3. Run the backend

```bash
cd backend
./mvnw spring-boot:run
```

On Windows PowerShell:

```bash
cd backend
.\mvnw spring-boot:run
```

The backend should run at:

```text
http://localhost:8080
```

Health check endpoint:

```text
http://localhost:8080/actuator/health
```

### 4. Run the frontend

```bash
cd frontend
npm install
npm start
```

The frontend should run at:

```text
http://localhost:4200
```

## Database Migrations 🗃️

Database schema changes are managed with Flyway.

Migration files live inside the backend:

```text
backend/src/main/resources/db/migration/
```

Example:

```text
V1__create_initial_schema.sql
V2__create_uploaded_files_table.sql
V3__create_processing_jobs_table.sql
V4__create_validation_errors_table.sql
```

Flyway automatically applies pending migrations when the backend starts.

## Planned API Endpoints 🔌

Initial planned endpoints:

```text
POST   /api/report-files
GET    /api/processing-jobs
GET    /api/processing-jobs/{id}
GET    /api/processing-jobs/{id}/validation-errors
GET    /api/reports
GET    /api/reports/{id}/download
GET    /api/audit-logs
```

These endpoints may change as the MVP evolves.

## MVP Roadmap 🧭

### Phase 1 — Project Foundation

- Monorepo structure
- Angular frontend setup
- Spring Boot backend setup
- Root documentation
- AI agent instructions
- Git ignore rules

### Phase 2 — Backend Foundation

- PostgreSQL datasource configuration
- Flyway setup
- Initial database migrations
- Base package structure
- Actuator health check

### Phase 3 — Upload Workflow

- Excel file upload endpoint
- File metadata persistence
- Processing job creation
- Basic validation response

### Phase 4 — Frontend Upload UI

- Application shell layout
- Upload page
- File selector/dropzone
- Upload status
- Success and error feedback

### Phase 5 — Processing and Validation

- Layout validation
- Business rule validation
- Validation error table
- Processing history screen

### Phase 6 — Reporting and Audit

- Downloadable report output
- Audit log tracking
- Report list and detail pages

## Testing 🧪

### Frontend

```bash
cd frontend
npm test
npm run build
```

### Backend

```bash
cd backend
./mvnw test
```

On Windows PowerShell:

```bash
cd backend
.\mvnw test
```

Future backend integration tests may use Testcontainers with PostgreSQL.

## Development Workflow 🔄

Recommended local workflow:

```text
VS Code      → Angular frontend
IntelliJ IDEA → Spring Boot backend
Docker Compose → PostgreSQL and local services
Codex / AI tools → Guided implementation using AGENTS.md
```

## Commit Convention 📝

This project follows Conventional Commits.

Examples:

```text
chore: initialize regulatory reporting platform monorepo
feat: add report upload endpoint
feat: add Excel upload page
fix: handle invalid Excel layout errors
docs: add architecture overview
test: add validation service tests
refactor: extract validation service
chore: configure Docker Compose
```

Commits should represent a feature or logical change, not simply a folder such as frontend or backend.

## Documentation 📚

Additional documentation will live under:

```text
docs/
```

Planned documentation:

- Architecture overview
- Database model
- Business rules
- API specification
- Deployment guide
- Testing strategy
- Screenshots

## Purpose 🎯

This project is intended to demonstrate the ability to design and build a realistic enterprise-grade full-stack system.

It focuses on maintainability, business logic, validation workflows, database evolution, API design, frontend architecture, testing, and developer experience.

## License 📄

This project is currently intended for portfolio and educational purposes.