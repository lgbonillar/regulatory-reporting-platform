
You are an expert in TypeScript, Angular, and scalable web application development. You write functional, maintainable, performant, and accessible code following Angular and TypeScript best practices.

## TypeScript Best Practices

- Use strict type checking
- Prefer type inference when the type is obvious
- Avoid the `any` type; use `unknown` when type is uncertain

## Angular Best Practices

- Always use standalone components over NgModules
- Must NOT set `standalone: true` inside Angular decorators. It's the default in Angular v20+.
- Use signals for state management
- Implement lazy loading for feature routes
- Do NOT use the `@HostBinding` and `@HostListener` decorators. Put host bindings inside the `host` object of the `@Component` or `@Directive` decorator instead
- Use `NgOptimizedImage` for all static images.
  - `NgOptimizedImage` does not work for inline base64 images.

## Accessibility Requirements

- It MUST pass all AXE checks.
- It MUST follow all WCAG AA minimums, including focus management, color contrast, and ARIA attributes.

### Components

- Keep components small and focused on a single responsibility
- Use `input()` and `output()` functions instead of decorators
- Use `computed()` for derived state
- Set `changeDetection: ChangeDetectionStrategy.OnPush` in `@Component` decorator
- Prefer inline templates for small components
- Prefer Reactive forms instead of Template-driven ones
- Do NOT use `ngClass`, use `class` bindings instead
- Do NOT use `ngStyle`, use `style` bindings instead
- When using external templates/styles, use paths relative to the component TS file.

## State Management

- Use signals for local component state
- Use `computed()` for derived state
- Keep state transformations pure and predictable
- Do NOT use `mutate` on signals, use `update` or `set` instead

## Templates

- Keep templates simple and avoid complex logic
- Use native control flow (`@if`, `@for`, `@switch`) instead of `*ngIf`, `*ngFor`, `*ngSwitch`
- Use the async pipe to handle observables
- Do not assume globals like (`new Date()`) are available.

## Services

- Design services around a single responsibility
- Use the `providedIn: 'root'` option for singleton services
- Use the `inject()` function instead of constructor injection

## Project Context

This frontend belongs to a private enterprise regulatory reporting platform.

The application is focused on:

- Excel file uploads
- Financial data validation
- Regulatory report processing
- Business rule execution
- Processing history
- Validation error dashboards
- Audit logs
- Downloadable reports
- Role-based access control

This is a client-side rendered Angular SPA.

Do not add SSR or SSG unless explicitly requested.

## Project Folder Structure

Use the following structure under `src/app`:

```text
src/app/
├── core/
├── layout/
├── shared/
└── features/
```

### `core/`

Use `core/` for application-wide singleton logic:

- Authentication
- Authorization guards
- HTTP interceptors
- App configuration
- Global services
- Error handling
- Loading state services
- Notification services

Do not place feature-specific logic in `core/`.

### `layout/`

Use `layout/` for application shell components:

- Main shell
- Sidebar
- Topbar
- Footer
- Navigation layout

Layout components should not contain business logic.

### `shared/`

Use `shared/` for reusable, generic UI and utility code:

- Presentational components
- Pipes
- Directives
- Generic models
- Utility functions

Do not place feature-specific components in `shared/`.

### `features/`

Use `features/` for business functionality grouped by domain.

Recommended feature folders:

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

Each feature should follow this structure when applicable:

```text
feature-name/
├── pages/
├── components/
├── services/
├── models/
└── feature-name.routes.ts
```

## Naming Conventions

Use business-oriented and readable names.

Page components should use the `*-page` suffix.

Examples:

```text
upload-report-page
processing-history-page
processing-detail-page
reports-list-page
report-detail-page
audit-log-page
users-list-page
```

Feature components should use descriptive names.

Examples:

```text
excel-dropzone
layout-preview
upload-result-summary
processing-status-badge
validation-errors-table
report-download-actions
audit-log-table
role-selector
```

Services should use the `*.service.ts` suffix.

Models should use the `*.model.ts` suffix.

Route files should use the `*.routes.ts` suffix.

## Routing Guidelines

- Use lazy-loaded routes for each major feature.
- Keep feature routes inside their own `feature-name.routes.ts` file.
- Keep the main `app.routes.ts` focused on top-level route composition.
- Use route guards for authentication and role-based access control.
- Do not put business logic directly inside guards.

## API and HTTP Guidelines

- Use typed request and response models for all backend communication.
- Keep HTTP calls inside feature services.
- Do not call `HttpClient` directly from components.
- Use functional HTTP interceptors.
- Keep API base URLs in environment configuration.
- Handle loading, success, empty, and error states explicitly.
- Do not swallow HTTP errors silently.
- Map backend DTOs to UI models when the backend response shape is not ideal for the view.

## Forms Guidelines

- Prefer typed Reactive Forms for production features.
- Do not use Template-driven Forms for complex business flows.
- Do not use experimental form APIs unless explicitly requested.
- Keep validation rules readable and close to the form model.
- Show validation messages only after a field is touched, dirty, or after submit.
- Disable submit buttons while a request is in progress.

## State Management Guidelines

- Use signals for local UI state.
- Use `computed()` for derived UI state.
- Use RxJS for HTTP streams and async workflows when it improves clarity.
- Avoid adding global state management libraries unless there is a clear need.
- Keep state transformations pure and predictable.

Examples of local UI state:

- Loading indicators
- Selected item
- Current tab
- Filters
- Modal visibility
- Upload progress
- Validation result summary

## Security Guidelines

- Never store secrets, private keys, database passwords, or production tokens in the frontend.
- Do not hardcode JWT tokens.
- Do not bypass Angular sanitization unless explicitly justified.
- Validate file type and size in the UI, but never assume frontend validation is enough.
- Authorization decisions must be enforced by the backend.
- Frontend guards are only for user experience, not real security.

## Excel Upload Guidelines

For file upload flows:

- Validate allowed extensions before upload.
- Show selected file name and size.
- Show upload progress when available.
- Show clear validation feedback.
- Display processing result summaries.
- Display validation errors in a table when available.
- Include row, column, field, message, and severity for validation errors when the backend provides them.
- Allow downloading generated reports only after successful processing.

## UI/UX Guidelines

- Every page should handle loading, empty, error, and success states.
- Tables should support clear empty states.
- Destructive actions should require confirmation.
- Error messages should be understandable for business users.
- Avoid exposing raw technical errors directly in the UI.
- Prefer clear enterprise-style screens over flashy visual effects.

## Testing Guidelines

- Add or update tests for meaningful logic.
- Test services that transform API responses.
- Test guards and interceptors when modified.
- Test components with important conditional rendering.
- Prefer testing behavior over implementation details.
- Do not add superficial tests that only check component creation unless no better test is meaningful yet.

Before considering frontend work complete, run:

```bash
npm run build
npm test
```

If a command is missing, failing due to missing setup, or not configured yet, mention it clearly.

## Code Review Checklist

Before finishing a change, verify:

- The feature follows the folder structure.
- Components remain small and focused.
- No `any` was introduced.
- No business logic was placed directly in templates.
- HTTP calls are not made directly from components.
- Loading and error states are handled.
- Accessibility basics are respected.
- The app builds successfully.
- Tests were added or updated when appropriate.
