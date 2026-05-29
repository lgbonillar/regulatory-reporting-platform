# AGENTS.md

## Scope

This folder stores Excel samples used by demo and validation flows.

## Working Rules

- Keep samples focused on business scenarios, not random data.
- Preserve deterministic behavior for repeatable local tests.
- Avoid renaming or replacing files referenced by Postman/docs without updating those references.

## Coverage Expectations

Maintain a balanced set of examples:

- Valid files that should pass upload validation
- Invalid files that should produce findings
- Files useful for processing success/failure demos

## File Hygiene

- Do not include sensitive or real production data.
- Use synthetic/demo-safe content only.
- Keep files reasonably small unless large-volume behavior is explicitly being tested.

## Cross-Area Alignment

When sample behavior changes:

- Update `samples/README.md`.
- Update `postman/` assets if they rely on those files.
- Update `docs/business-rules.md` or `docs/api-spec.md` when outcomes or validation assumptions change.
