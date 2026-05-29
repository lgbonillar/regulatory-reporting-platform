# Frontend

Angular client application for the Regulatory Reporting Platform.

This project is a client-side rendered enterprise SPA focused on:

- Excel file upload
- Processing job tracking
- Validation findings
- Role-based navigation
- Audit-friendly detail views
- Modular UI components
- Future JWT-based authentication flows

## Tech Stack

- Angular
- TypeScript
- PrimeNG
- Tailwind CSS
- Font Awesome
- Standalone components
- Angular signals
- Reactive Forms

## Prerequisites

- [nvm](https://github.com/nvm-sh/nvm)
- Node.js `24.16.0`
- npm

### Node Version

This project uses `nvm` to pin the Node.js version.

From the `frontend/` directory:

```bash
nvm install
nvm use
```

If you already have the correct version installed, nvm use will switch to it using the .nvmrc file.

## Local Development

### Install dependencies

```bash
npm install
```

### Run the development server

```bash
npm start
```

The app runs at: `http://localhost:4200`

### Build for production
```
npm run build
```

### Run tests

```
npm test
```

## Project Structure

```text
src/app/
├── core/      # singleton services, guards, interceptors,
├── layout/    # shell, topbar, sidenav, page chrome
├── shared/    # reusable UI pieces, pipes, directives
└── features/  # business screens by domain
```

### core/

Application-wide singleton logic:

- authentication
- guards
- interceptors
- navigation config
- theme config
- global services

### layout/

Application shell and structural UI:

- main shell
- top bar
- side navigation
- responsive layout containers

### shared/

Reusable UI and utility code:

- generic components
- pipes
- directives
- helper utilities
- shared models

### features/

Business features grouped by domain:

- auth/
- file-upload/
- processing-jobs/
- forbidden/

## Key UI Principles

- Keep components small and focused.
- Prefer reusable components over duplicated markup.
- Use signals for local UI state.
- Use Reactive Forms for forms.
- Keep API calls inside services.
- Avoid putting business logic in components.
- Keep loading, empty, error, and success states explicit.

## Naming Conventions

- Page components use the *-page suffix.
- Service files use the *.service.ts suffix.
- Models use the *.model.ts suffix.
- Route files use the *.routes.ts suffix.

Examples:

upload-report-page
processing-jobs-page
processing-job-details-page
report-file-upload.service.ts
processing-job.model.ts
processing-jobs.routes.ts

## Routing

- Use lazy-loaded feature routes.
- Keep route definitions inside each feature folder.
- Keep app.routes.ts focused on top-level composition.
- Use guards for authentication and role-based access control.

## HTTP Guidelines

- Use typed request and response models.
- Keep HttpClient usage inside services.
- Map backend DTOs to UI models when needed.
- Handle loading and error states explicitly.
- Do not swallow HTTP errors silently.

## Accessibility

The frontend should follow basic enterprise accessibility standards:

- visible focus states
- good contrast
- keyboard support
- semantic HTML
- tooltips used only where they add value
- ARIA labels where appropriate

## Related Docs

- ../README.md
- ./AGENTS.md
- ../docs/architecture.md
- ../docs/api-spec.md
- ../docs/business-rules.md

## Development Notes

- SSR and SSG are not part of this application.
- The UI should stay enterprise-focused and stable.
- Keep the layout modular so future features can be added without rewriting the shell.

## Common Issues

### Build fails after dependency changes

Delete node_modules and reinstall dependencies:

rm -rf node_modules
npm install

### Port already in use

Stop the process using port 4200 or change the local Angular port in the project configuration.

## Contribution Flow

1. Review the relevant docs.
2. Make the smallest change needed.
3. Keep UI, services, and models separated.
4. Update tests if behavior changes.
5. Verify the app builds and tests pass.
