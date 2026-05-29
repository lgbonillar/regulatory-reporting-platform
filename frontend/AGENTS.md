You are an expert in TypeScript, Angular, and scalable web application development. You write functional, maintainable, performant, and accessible code following Angular and TypeScript best practices.

## Instruction Precedence

Project-specific rules in this `AGENTS.md` override external skills when there is a conflict.

External Angular skills may be used for framework knowledge, documentation lookup, examples, and modern Angular guidance, but this project keeps its own architectural decisions.

When an external skill recommends an API or pattern that conflicts with this file, follow this file unless the user explicitly requests otherwise.

## TypeScript Best Practices

- Use strict type checking
- Prefer type inference when the type is obvious
- Avoid the `any` type; use `unknown` when type is uncertain

## Angular Best Practices

- Always use standalone components over NgModules
- Must NOT set `standalone: true` inside Angular decorators for Angular 20+ projects. It is the default in modern Angular.
- If the installed Angular version is older than Angular 20, verify the project configuration before removing or omitting `standalone: true`.
- Use signals for state management
- Implement lazy loading for feature routes
- Do NOT use the `@HostBinding` and `@HostListener` decorators. Put host bindings inside the `host` object of the `@Component` or `@Directive` decorator instead
- Use `NgOptimizedImage` for all static images.
  - `NgOptimizedImage` does not work for inline base64 images.

## Angular Version Target

This project targets modern Angular, preferably Angular 20+ / 21+.

Before applying Angular-specific migrations or syntax changes, verify the installed Angular version in `package.json`.

Do not apply rules that require Angular 19+ or Angular 21+ to older Angular projects unless the task explicitly includes a framework upgrade.

## Accessibility Requirements

- Prefer semantic HTML before adding ARIA.
- Do not add ARIA attributes that duplicate or conflict with native semantics.
- Ensure keyboard navigation works for interactive components.
- Ensure focus is managed after dialogs, route changes, destructive actions, and upload flows.
- Custom interactive components must support keyboard interaction, visible focus, and screen reader labels.

## Modern Angular Runtime Guidelines

- Prefer code compatible with Angular 20+ and Angular 21+.
- Verify the installed Angular version before applying version-specific syntax or migrations.
- Prefer zoneless-compatible patterns.
- Do not rely on ZoneJS side effects for UI updates.
- Prefer signals, computed state, async pipe, `toSignal()`, component inputs, and template/host events to notify Angular of state changes.
- Use `effect()` only for side effects, not for propagating state.
- Use `takeUntilDestroyed()` for manual RxJS subscriptions.
- Do not use experimental Angular APIs in production unless explicitly requested.
- Signal Forms, `resource`, `rxResource`, and Angular Aria should be treated as experimental or preview unless the project explicitly adopts them.

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

### Component Class Members

- Mark `input()`, `output()`, `model()`, queries, and injected dependencies as `readonly` when they should not be reassigned.
- Use `protected` for component members that are only accessed from the template.
- Keep public members only for APIs intentionally consumed from outside the component.

### Component and Template Style

- Name event handlers for what they do, not for the triggering event.
- Prefer names like `saveUserData()`, `submitUpload()`, or `confirmDelete()` instead of generic names like `handleClick()`.
- Keep lifecycle hooks simple.
- Move complex initialization or side-effect logic into well-named private or protected methods.
- Implement lifecycle interfaces such as `OnInit`, `OnDestroy`, or `AfterViewInit` when lifecycle hooks are used.

## Templates

- Keep templates simple and avoid complex logic.
- Use native control flow (`@if`, `@for`, `@switch`) instead of `*ngIf`, `*ngFor`, `*ngSwitch`.
- Always provide a `track` expression when using `@for`.
- Prefer stable unique identifiers for `@for` tracking, such as `item.id` or `item.uuid`.
- Use `track $index` only for static collections that never reorder, add, or remove items.
- Prefer `@empty` inside `@for` blocks for empty states when appropriate.
- Use `@let` for readable local template values when it reduces repeated expressions.
- Do not place complex business logic directly in templates; move it to TypeScript, preferably using `computed()` when it represents derived state. - Do not assume globals like (`new Date()`) are available.

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

## Project Naming Conventions

Although modern Angular no longer requires suffixes for every artifact, this project intentionally uses explicit suffixes for enterprise readability.

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
- Use `loadComponent` for lazy-loaded standalone pages.
- Use `loadChildren` for lazy-loaded feature route trees.
- Keep primary landing routes eager only when it improves initial UX.
- Avoid excessive nested lazy loading when it adds unnecessary navigation latency.

## API and HTTP Guidelines

- Use typed request and response models for all backend communication.
- Keep HTTP calls inside feature services.
- Do not call `HttpClient` directly from components.
- Use functional HTTP interceptors.
- Keep API base URLs in environment configuration.
- Handle loading, success, empty, and error states explicitly.
- Do not swallow HTTP errors silently.
- Map backend DTOs to UI models when the backend response shape is not ideal for the view.
- Be careful with `withFetch()` in file upload flows. Angular's fetch-based HttpClient configuration does not produce upload progress events.
- For upload flows that need progress reporting, use the default XHR-based HttpClient behavior for those requests.

## Forms Guidelines

- Prefer strictly typed Reactive Forms for production enterprise flows.
- Do not use Template-driven Forms for complex business flows.
- Do not use Signal Forms in production features unless explicitly requested and approved.
- Signal Forms are experimental in Angular v21.
- Avoid `UntypedFormGroup`, `UntypedFormControl`, and `UntypedFormArray` unless migrating legacy code.
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

### Signals Effects

- Prefer `computed()` for derived state.
- Do not use `effect()` to copy state from one signal to another.
- Use `effect()` only for side effects such as logging, syncing to storage, analytics, or integrating with imperative third-party APIs.
- Avoid async logic inside `effect()` unless signal reads are performed before the async boundary.

## RxJS and Signals Interop

- Use the async pipe for simple observable consumption in templates.
- Use `toSignal()` when observable data needs to participate in signal-based derived state.
- Do not call `toSignal()` repeatedly for the same Observable; reuse the returned signal.
- Use `takeUntilDestroyed()` for manual subscriptions that cannot be handled by the async pipe or `toSignal()`.

## Zoneless Compatibility

- Prefer code that is compatible with zoneless Angular.
- Do not rely on ZoneJS side effects to update the UI.
- UI updates should be triggered through signals, async pipe, component inputs, template/host events, or explicit change detection APIs when needed.
- For Angular v21+ projects, do not add `zone.js` unless explicitly required.
- For existing apps, do not migrate to zoneless without checking current dependencies and behavior.

## Security Guidelines

- Never store secrets, private keys, database passwords, or production tokens in the frontend.
- Do not hardcode JWT tokens.
- Do not log tokens, credentials, personal data, uploaded file contents, or regulatory data.
- Do not expose backend validation details that could leak internals.
- Do not bypass Angular sanitization unless explicitly justified.
- Avoid `[innerHTML]` unless the content is trusted, sanitized, and explicitly justified.
- Do not disable Angular sanitization.
- Validate file type and size in the UI, but never assume frontend validation is enough.
- Authorization decisions must be enforced by the backend.
- Frontend guards and role checks are only for user experience, navigation, and conditional rendering.

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

## Agent Workflow Guidelines

- Before making Angular-specific changes, inspect `package.json`, `angular.json`, and the existing folder structure.
- Prefer consistency with nearby files over introducing a new pattern.
- Do not run framework migrations unless explicitly requested.
- Do not add new dependencies unless they are clearly justified by the task.
- Do not introduce global state management libraries unless the need is explicit.
- Prefer small, incremental changes over large rewrites.
- When modifying an existing feature, preserve its current public behavior unless the task explicitly requests a behavior change.

## External Angular Skill Usage

The official Angular `angular-developer` skill may be used as a reference for modern Angular guidance.

However, for this project:

- Do not migrate production forms to Signal Forms unless explicitly requested and approved.
- Use typed Reactive Forms as the default for production enterprise forms.
- Do not replace `HttpClient` feature services with `resource`, `rxResource`, or `httpResource` unless explicitly requested and approved.
- Do not add SSR or SSG unless explicitly requested.
- Do not adopt Angular Aria as a default dependency unless the project explicitly decides to use it.
- Prefer `npm run build` over raw `ng build` when package scripts exist.
- Prefer the commands defined in `package.json` over generic Angular CLI commands.

## Testing Guidelines

- Add or update tests for meaningful logic.
- Test services that transform API responses.
- Test guards and interceptors when modified.
- Test components with important conditional rendering.
- Prefer testing behavior over implementation details.
- Do not add superficial tests that only check component creation unless no better test is meaningful yet.
- Do not assume the project uses Karma, Jasmine, Jest, or Vitest. Check `package.json` and Angular workspace configuration first.

Before considering frontend work complete, inspect `package.json` and run the available quality gates.

Common commands include: 

```bash
npm run build
npm run lint
npm test
```

For automated or agent-driven runs, prefer a non-watch test command when supported by the project, for example:

```bash
npm test -- --watch=false
```

If a command is missing, unsupported, failing due to missing setup, or not configured yet, mention it clearly.

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
