import { HttpClient } from '@angular/common/http'
import { inject, Injectable } from '@angular/core'
import { Observable } from 'rxjs'

import { environment } from '../../../../environments/environments.dev'
import { ReportFileUploadResponse, UploadedFileResponse } from '../models/report-file-upload.model'

@Injectable({
  providedIn: 'root'
})
export class ReportFileUploadService {
  private readonly httpClient = inject(HttpClient)
  private readonly endpointUrl = `${environment.apiBaseUrl}/api/report-files`

  listReportFiles (username: string): Observable<UploadedFileResponse[]> {
    return this.httpClient.get<UploadedFileResponse[]>(this.endpointUrl, {
      params: { username }
    })
  }

  uploadReportFile (file: File): Observable<ReportFileUploadResponse> {
    const formData = new FormData()
    formData.append('file', file)

    return this.httpClient.post<ReportFileUploadResponse>(this.endpointUrl, formData)
  }

  updateReportFile (fileId: string, file: File): Observable<ReportFileUploadResponse> {
    const formData = new FormData()
    formData.append('file', file)

    return this.httpClient.put<ReportFileUploadResponse>(`${this.endpointUrl}/${fileId}`, formData)
  }

  deleteReportFile (fileId: string): Observable<void> {
    return this.httpClient.delete<void>(`${this.endpointUrl}/${fileId}`)
  }

  getDownloadUrl (fileId: string): string {
    return `${this.endpointUrl}/${fileId}/download`
  }
}
