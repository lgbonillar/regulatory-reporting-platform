# Contributing to Regulatory Reporting Platform

Thank you for taking the time to contribute to this project.

This document describes the workflow, coding standards, commit rules, and validation steps for the Regulatory Reporting Platform.

## Table of Contents

- [Project Overview](#project-overview)
- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Branch Naming](#branch-naming)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Coding Guidelines](#coding-guidelines)
- [Testing Guidelines](#testing-guidelines)
- [Documentation Guidelines](#documentation-guidelines)
- [Pull Request Guidelines](#pull-request-guidelines)
- [Security Guidelines](#security-guidelines)

## Project Overview

Regulatory Reporting Platform is a full-stack application for Excel file uploads, regulatory report processing, workflow tracking, approval flows, and auditability.

The repository is organized as a monorepo:

```text
regulatory-reporting-platform/
├── frontend/   # Angular client application
├── backend/    # Spring Boot REST API
├── database/   # Database scripts and migrations
├── docs/       # Technical documentation
├── samples/    # Sample Excel files and generated outputs
└── postman/    # API collections and environments
```

Keep frontend and backend concerns separated. Changes should be small, focused, and easy to review.

## Code of Conduct

Be respectful, precise, and constructive.

Technical feedback should focus on the code, architecture, tests, and maintainability. Avoid personal criticism and avoid introducing unnecessary scope into a contribution.

## Getting Started

### Prerequisites

Install the following tools:

- Node.js
- npm
- Java 21
- Maven 3.9.15
- Docker
- Docker Compose
- SDKMAN, recommended for backend version management

The backend includes an SDKMAN configuration file:

```text
backend/.sdkmanrc
```

From the backend directory, SDKMAN should activate:

```bash
cd backend
sdk env
```

Verify the versions:

```bash
java -version
mvn -version
```

Expected backend versions:

```text
Java 21
Maven 3.9.15
```

### Install Frontend Dependencies

```bash
cd frontend
npm install
```

### Start Local Infrastructure

From the repository root:

```bash
docker compose up -d
```

### Run the Backend

```bash
cd backend
mvn spring-boot:run
```

If Maven is not installed locally, use the Maven Wrapper:

```bash
cd backend
./mvnw spring-boot:run
```

### Run the Frontend

```bash
cd frontend
npm start
```

## Development Workflow

1. Make sure your local branch is up to date.
2. Create a focused branch for your change.
3. Keep commits small and logically grouped.
4. Add or update tests for behavior changes.
5. Update documentation when architecture, setup, APIs, or business behavior changes.
6. Run the relevant validation commands before opening a pull request.

Avoid mixing unrelated changes in the same branch. For example, do not combine a UI refactor, database migration, and authentication change unless they are part of one coherent feature.

## Branch Naming

Use short names based on the type of work:

```text
feat/add-user-management
fix/block-invalid-job-transition
test/add-processing-workflow-tests
refactor/split-upload-services
docs/update-api-spec
build/configure-sdkman
```

## Commit Message Guidelines

Use an Angular-style commit convention.

Format:

```text
<type>(<scope>): <summary>
```

```text
  <type>(<scope>): <summary>
    │      │          │
    │      │          └─⫸ Summary in present tense. Lowercase, no accents, no final period.
    │      │
    │      └─⫸ Commit scope: frontend|backend|database|docs|samples|postman|core|users|upload|processing|reports|audit
    │
    └─⫸ Commit type: build|docs|feat|fix|refactor|test
```

Examples:

```text
feat(upload): add report upload endpoint
fix(processing): block approval for missing files
docs(backend): update setup instructions
test(processing): add workflow service tests
refactor(upload): split uploaded file services
build(backend): add sdkman environment config
```

### Common Commit Types

- `feat`: user-facing feature or business capability
- `fix`: bug fix
- `docs`: documentation-only change
- `test`: test-only change
- `refactor`: code restructuring without changing behavior
- `build`: build system, dependency, or toolchain change

Do not use other commit types unless the convention is explicitly updated.

### Commit Scopes

Use one of these scopes:

- `frontend`: Angular application-wide change
- `backend`: Spring Boot API-wide change
- `database`: database scripts, schema, or migrations
- `docs`: repository documentation
- `samples`: sample files or generated outputs
- `postman`: Postman collections or environments
- `core`: shared application foundation or cross-cutting behavior
- `users`: users, roles, identity, or future authentication behavior
- `upload`: uploaded files and file metadata
- `processing`: processing jobs, workflow, status history, and approvals
- `reports`: generated reports and report exports
- `audit`: audit logs and traceability

### Commit Rules

- Use present tense: `add`, `fix`, `update`, `remove`.
- Use lowercase in the summary.
- Do not use accents in the summary.
- Do not add a period at the end.
- Keep the first line concise, preferably under 100 characters.
- Do not use vague messages like `changes`, `fix stuff`, or `wip`.
- Prefer business language when possible.
- Do not commit secrets, tokens, private keys, passwords, or production credentials.

## Coding Guidelines

### General

- Keep changes scoped to the affected area.
- Prefer existing project patterns over introducing new abstractions.
- Use clear names based on business concepts.
- Avoid unnecessary dependencies.
- Keep generated files, local environment files, and build outputs out of commits unless explicitly required.

### Backend

The backend is a Spring Boot REST API organized by business domain.

Preferred package structure:

```text
feature/
├── controller
├── application
├── domain
├── infrastructure
└── dto
```

Backend guidelines:

- Keep controllers thin.
- Put orchestration in application services.
- Put business invariants in domain objects or domain services.
- Keep persistence concerns in repositories and infrastructure classes.
- Use DTOs for API requests and responses.
- Use Flyway migrations for schema changes.
- Do not edit existing Flyway migrations after they have been applied. Add a new migration instead.
- Throw meaningful domain or application exceptions for invalid business operations.

### Frontend

The frontend is an Angular application.

Frontend guidelines:

- Keep feature code inside `frontend/src/app/features`.
- Keep reusable UI and utilities inside `shared`.
- Keep app-wide services, interceptors, guards, and configuration inside `core`.
- Prefer typed models and explicit interfaces.
- Keep components focused on presentation and user interaction.
- Move API calls and state coordination into services when the component starts taking too much responsibility.
- Preserve accessibility basics: labels, button types, keyboard-friendly interactions, and readable status text.

## Testing Guidelines

Tests should match the risk of the change.

### Backend Tests

For domain rules:

```bash
cd backend
mvn test -Dtest=ProcessingJobTest,UploadedFileTest
```

For application service behavior:

```bash
cd backend
mvn test -Dtest=ProcessingJobWorkflowServiceTest
```

For the full backend test suite:

```bash
cd backend
mvn test
```

Some backend tests may require Docker because the Spring context uses Testcontainers.

### Frontend Tests

Build the frontend:

```bash
cd frontend
npm run build
```

Run frontend tests:

```bash
cd frontend
npm test
```

Run linting:

```bash
cd frontend
npm run lint
```

## Documentation Guidelines

Update documentation when changing:

- Setup instructions
- Environment requirements
- API behavior
- Database schema
- Business rules
- Architecture decisions
- User-facing workflows

Relevant documentation lives in:

```text
docs/
README.md
CONTRIBUTING.md
postman/
```

## Pull Request Guidelines

Before opening a pull request:

- Make sure the branch has a clear purpose.
- Keep the PR focused.
- Confirm relevant tests pass.
- Include database migrations when schema changes are required.
- Update documentation when behavior or setup changes.
- Explain any known limitations or follow-up work.

A good PR description should include:

```text
## Summary

Briefly describe the change.

## Validation

- [ ] Backend tests pass
- [ ] Frontend build passes
- [ ] Frontend tests pass
- [ ] Manual verification completed, if needed

## Notes

Mention migrations, breaking changes, risks, or follow-up work.
```

## Security Guidelines

- Never commit secrets, tokens, passwords, private keys, or production credentials.
- Use `.env.example` for documenting required environment variables.
- Keep local `.env` files out of version control.
- Do not log sensitive user data.
- Be careful with uploaded files: validate file type, size, storage path, and processing status.
- Treat auditability as a core requirement for workflow changes.
