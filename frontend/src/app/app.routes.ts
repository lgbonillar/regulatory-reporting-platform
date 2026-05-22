import { Routes } from '@angular/router'

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'report-files/upload'
  },
  {
    path: 'report-files/upload',
    loadChildren: () => import('./features/file-upload/file-upload.routes').then((routes) => routes.FILE_UPLOAD_ROUTES)
  },
  {
    path: 'processing-jobs',
    loadChildren: () => import('./features/processing-jobs/processing-jobs.routes').then((routes) => routes.PROCESSING_JOBS_ROUTES)
  }
]
