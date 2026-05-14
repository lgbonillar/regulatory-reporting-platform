import { HttpClient } from '@angular/common/http'
import { inject, Injectable } from '@angular/core'
import { Observable } from 'rxjs'

import { environment } from '../../../../environments/environments.dev'
import { ReportFileUploadResponse } from '../models/report-file-upload.model'

@Injectable({
  providedIn: 'root'
})
export class ReportFileUploadService {
  private readonly httpClient = inject(HttpClient)
  private readonly endpointUrl = `${environment.apiBaseUrl}/api/report-files`

  uploadReportFile (file: File): Observable<ReportFileUploadResponse> {
    const formData = new FormData()
    formData.append('file', file)

    return this.httpClient.post<ReportFileUploadResponse>(this.endpointUrl, formData)
  }

  getDownloadUrl (fileId: string): string {
    return `${this.endpointUrl}/${fileId}/download`
  }
}
