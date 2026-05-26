import { Routes } from '@angular/router'

import { authGuard } from './core/auth/auth.guard'
import { roleGuard } from './core/auth/role.guard'
import { MainShell } from './layout/main-shell/main-shell'

export const routes: Routes = [
  {
    path: 'login',
    loadChildren: () => import('./features/auth/auth.routes').then((routes) => routes.AUTH_ROUTES)
  },
  {
    path: '',
    component: MainShell,
    canActivate: [ authGuard ],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'report-files/upload'
      },
      {
        path: 'report-files/upload',
        canActivate: [ roleGuard ],
        data: { roles: [ 'ANALYST' ] },
        loadChildren: () => import('./features/file-upload/file-upload.routes').then((routes) => routes.FILE_UPLOAD_ROUTES)
      },
      {
        path: 'processing-jobs',
        canActivate: [ roleGuard ],
        data: { roles: [ 'ANALYST', 'ADMINISTRATOR' ] },
        loadChildren: () => import('./features/processing-jobs/processing-jobs.routes').then((routes) => routes.PROCESSING_JOBS_ROUTES)
      }
    ]
  }
]
