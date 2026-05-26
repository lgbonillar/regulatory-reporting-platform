import { Routes } from '@angular/router'

import { authGuard } from './core/auth/auth.guard'
import { defaultRouteGuard } from './core/auth/default-route.guard'
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
        canActivate: [ defaultRouteGuard ],
        children: []
      },
      {
        path: 'forbidden',
        loadComponent: () => import('./features/forbidden/forbidden-page').then((page) => page.ForbiddenPage)
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
  },
  {
    path: '**',
    redirectTo: ''
  }
]
