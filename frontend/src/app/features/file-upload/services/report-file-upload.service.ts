import { HttpClient } from '@angular/common/http'
import { inject, Injectable } from '@angular/core'
import { Observable } from 'rxjs'

import { environment } from '../../../../environments/environments.dev'
import { ApiResponse, unwrapApiResponse } from '../../../shared/utils/api-response'
import { ReportFileUploadResponse, UploadedFileResponse } from '../models/report-file-upload.model'

@Injectable({
  providedIn: 'root'
})
export class ReportFileUploadService {
  private readonly httpClient = inject(HttpClient)
  private readonly endpointUrl = `${environment.apiBaseUrl}/api/report-files`

  listReportFiles (username: string): Observable<UploadedFileResponse[]> {
    return this.httpClient
      .get<ApiResponse<UploadedFileResponse[]>>(this.endpointUrl, {
        params: { username }
      })
      .pipe(unwrapApiResponse())
  }

  uploadReportFile (file: File): Observable<ReportFileUploadResponse> {
    const formData = new FormData()
    formData.append('file', file)

    return this.httpClient
      .post<ApiResponse<ReportFileUploadResponse>>(this.endpointUrl, formData)
      .pipe(unwrapApiResponse())
  }

  updateReportFile (fileId: string, file: File): Observable<ReportFileUploadResponse> {
    const formData = new FormData()
    formData.append('file', file)

    return this.httpClient
      .put<ApiResponse<ReportFileUploadResponse>>(`${this.endpointUrl}/${fileId}`, formData)
      .pipe(unwrapApiResponse())
  }

  deleteReportFile (fileId: string): Observable<void> {
    return this.httpClient.delete<void>(`${this.endpointUrl}/${fileId}`)
  }

}
