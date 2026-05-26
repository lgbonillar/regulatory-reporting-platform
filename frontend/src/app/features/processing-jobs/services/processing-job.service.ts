import { HttpClient } from '@angular/common/http'
import { inject, Injectable } from '@angular/core'
import { Observable } from 'rxjs'

import { environment } from '../../../../environments/environments.dev'
import { ApiResponse, unwrapApiResponse } from '../../../shared/utils/api-response'
import { ProcessingJobResponse, ProcessingJobStatusHistoryResponse } from '../models/processing-job.model'

@Injectable({
  providedIn: 'root'
})
export class ProcessingJobService {
  private readonly httpClient = inject(HttpClient)
  private readonly endpointUrl = `${environment.apiBaseUrl}/api/processing-jobs`

  listProcessingJobs (): Observable<ProcessingJobResponse[]> {
    return this.httpClient
      .get<ApiResponse<ProcessingJobResponse[]>>(this.endpointUrl)
      .pipe(unwrapApiResponse())
  }

  listProcessingJobsByUsername (username: string): Observable<ProcessingJobResponse[]> {
    return this.httpClient
      .get<ApiResponse<ProcessingJobResponse[]>>(this.endpointUrl, {
        params: { username }
      })
      .pipe(unwrapApiResponse())
  }

  getProcessingJob (jobId: string): Observable<ProcessingJobResponse> {
    return this.httpClient
      .get<ApiResponse<ProcessingJobResponse>>(`${this.endpointUrl}/${jobId}`)
      .pipe(unwrapApiResponse())
  }

  getProcessingJobHistory (jobId: string): Observable<ProcessingJobStatusHistoryResponse[]> {
    return this.httpClient
      .get<ApiResponse<ProcessingJobStatusHistoryResponse[]>>(
        `${this.endpointUrl}/${jobId}/history`
      )
      .pipe(unwrapApiResponse())
  }

  startProcessing (jobId: string): Observable<ProcessingJobResponse> {
    return this.httpClient
      .post<ApiResponse<ProcessingJobResponse>>(`${this.endpointUrl}/${jobId}/start`, {})
      .pipe(unwrapApiResponse())
  }

  approve (jobId: string): Observable<ProcessingJobResponse> {
    return this.httpClient
      .post<ApiResponse<ProcessingJobResponse>>(`${this.endpointUrl}/${jobId}/approve`, {})
      .pipe(unwrapApiResponse())
  }

  reject (jobId: string, reason: string): Observable<ProcessingJobResponse> {
    return this.httpClient
      .post<ApiResponse<ProcessingJobResponse>>(`${this.endpointUrl}/${jobId}/reject`, { reason })
      .pipe(unwrapApiResponse())
  }

  revoke (jobId: string, reason: string): Observable<ProcessingJobResponse> {
    return this.httpClient
      .post<ApiResponse<ProcessingJobResponse>>(`${this.endpointUrl}/${jobId}/revoke`, { reason })
      .pipe(unwrapApiResponse())
  }

}
