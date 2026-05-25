import { Routes } from '@angular/router'

import { ProcessingJobsPage } from './pages/processing-jobs-page/processing-jobs-page'

export const PROCESSING_JOBS_ROUTES: Routes = [
  {
    path: '',
    component: ProcessingJobsPage
  },
  {
    path: ':jobId',
    loadComponent: () =>
      import('./pages/processing-job-details-page/processing-job-details-page')
        .then((m) => m.ProcessingJobDetailsPage)
  }
]
