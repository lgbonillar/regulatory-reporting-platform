# AGENTS.md

## Repository Context

This is a full-stack regulatory reporting platform implemented as a monorepo:

- `frontend/`: Angular client application
- `backend/`: Spring Boot REST API
- `database/`: database scripts, migrations, and developer seeds
- `docs/`: source-of-truth specifications, ADRs, and API contracts
- `samples/`: sample Excel files used for validation and demo flows
- `postman/`: API collections and environments

## Working Model

Treat this repository as a documentation-driven spec.

- Read the relevant docs before implementing a feature.
- Prefer updating the spec or ADR first when the behavior changes.
- Keep changes incremental and scoped to one business flow at a time.
- Do not introduce new abstractions unless they clearly reduce complexity or match an existing pattern.
- Do not move files across major folders unless explicitly requested.

## Documentation Hierarchy

Use these docs in this order when adding or changing behavior:

- `docs/specs/`: business and product requirements
- `docs/adr/`: architectural decisions and tradeoffs
- `docs/api/`: endpoint contracts and payload expectations
- `README.md`: project overview, local setup, and current status

If a change affects user-visible behavior, validation rules, or API shape, update the docs in the same change when practical.

## Backend and Frontend Guidance

Always read the nested instructions before editing code in those areas:

- `backend/AGENTS.md`
- `frontend/AGENTS.md`

Those files contain the area-specific rules for package structure, testing, and implementation style.

## General Rules

- Keep frontend and backend concerns separated.
- Use clear names based on business concepts.
- Avoid unnecessary dependencies.
- Keep the repository compilable after each logical change.
- Avoid broad refactors unless they are needed to finish the requested work safely.

## Validation

Before considering work complete, run the relevant checks for the changed area.

- Backend work: follow `backend/AGENTS.md`
- Frontend work: follow `frontend/AGENTS.md`
- Database or seed changes: validate the SQL logically and ensure the scripts still match the current schema
- Documentation-only changes: review for consistency and broken references

If a command is missing, fails because the environment is incomplete, or is not configured yet, mention that clearly.


## Security

Do not commit secrets, passwords, private keys, tokens, local `.env` files, or production credentials.

## Goal

Keep the platform easy to run, easy to understand, and easy to extend with future regulatory modules without losing architectural context.
