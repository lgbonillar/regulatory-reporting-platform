# AGENTS.md

## Repository Context

This is a full-stack regulatory reporting platform.

The project is organized as a monorepo with:

- `frontend/`: Angular client application
- `backend/`: Spring Boot REST API
- `database/`: database scripts and migrations
- `docs/`: technical documentation
- `samples/`: sample Excel files and generated outputs
- `postman/`: API collections

## General Rules

- Keep frontend and backend concerns separated.
- Do not move files across major folders unless explicitly requested.
- Prefer small, focused, reviewable changes.
- Use clear naming based on business concepts.
- Avoid adding unnecessary dependencies.
- Never commit secrets, tokens, passwords, private keys, or production credentials.
- Keep documentation updated when architecture, setup, or public behavior changes.

## Commit Style

Use Conventional Commits:

```text
feat: add report upload endpoint
fix: handle invalid Excel layout
docs: add architecture overview
test: add validation service tests
refactor: extract report processing service
chore: configure Docker Compose
```

## Validation

Before considering work complete, run the relevant checks for the changed area.

For frontend changes:

```bash
cd frontend
npm run build
npm test
```

For backend changes:

```bash
cd backend
./mvnw test
./mvnw spring-boot:run
```

If a command is missing, failing due to missing setup, or not configured yet, mention it clearly.