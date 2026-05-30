import { Routes } from '@angular/router'

import { UploadReportPage } from './pages/upload-report-page/upload-report-page'
import { UploadedFileDetailsPage } from './pages/uploaded-file-details-page/uploaded-file-details-page'

export const FILE_UPLOAD_ROUTES: Routes = [
  {
    path: '',
    component: UploadReportPage
  },
  {
    path: ':fileId',
    component: UploadedFileDetailsPage
  }
]
