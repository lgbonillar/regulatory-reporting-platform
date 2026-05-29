# Samples

Sample Excel files used for upload validation and processing demos.

## Purpose

These files support local testing of:

- Upload acceptance/rejection
- Validation findings
- Processing workflow behavior
- Postman and frontend demo flows

## Current Files

- `1000-Registros-de-ventas.xlsx`
- `Lista-de-clientes-con-nombre-y-direccion.xlsx`
- `Listado-de-proveedores-y-contactos.xlsx`
- `invalid-sales-report.xlsx`

## Suggested Usage

- Use `invalid-sales-report.xlsx` to validate failure handling and findings visualization.
- Use one of the other files as positive flow candidates, depending on current module rules.

Actual acceptance depends on active backend validation rules and module configuration.

## Naming Guidance

When adding new samples:

- Keep file names descriptive and stable.
- Include both valid and invalid examples.
- Prefer deterministic content so test outcomes are reproducible.

## Change Discipline

If a sample file is added, removed, or replaced:

- Update this README.
- Update Postman instructions if they depend on that sample.
- Update backend/frontend docs if expected outcomes changed.
