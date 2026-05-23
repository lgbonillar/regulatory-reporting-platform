import { HttpClient } from '@angular/common/http'
import { inject, Injectable } from '@angular/core'
import { Observable } from 'rxjs'

import { environment } from '../../../../environments/environments.dev'
import { ProcessingJobResponse, ProcessingJobStatusHistoryResponse } from '../models/processing-job.model'

@Injectable({
  providedIn: 'root'
})
export class ProcessingJobService {
  private readonly httpClient = inject(HttpClient)
  private readonly endpointUrl = `${environment.apiBaseUrl}/api/processing-jobs`

  listProcessingJobs (): Observable<ProcessingJobResponse[]> {
    return this.httpClient.get<ProcessingJobResponse[]>(this.endpointUrl)
  }

  listProcessingJobsByUsername (username: string): Observable<ProcessingJobResponse[]> {
    return this.httpClient.get<ProcessingJobResponse[]>(this.endpointUrl, {
      params: { username }
    })
  }

  getProcessingJob (jobId: string): Observable<ProcessingJobResponse> {
    return this.httpClient.get<ProcessingJobResponse>(`${this.endpointUrl}/${jobId}`)
  }

  getProcessingJobHistory(jobId: string):
  Observable<ProcessingJobStatusHistoryResponse[]> {
    return this.httpClient.get<ProcessingJobStatusHistoryResponse[]>(
      `${this.endpointUrl}/${jobId}/history`
    )
  }

  startProcessing(jobId: string): Observable<ProcessingJobResponse> {
    return this.httpClient.post<ProcessingJobResponse>(`${this.endpointUrl}/
    ${jobId}/start`, {})
  }

  approve(jobId: string): Observable<ProcessingJobResponse> {
    return this.httpClient.post<ProcessingJobResponse>(`${this.endpointUrl}/
    ${jobId}/approve`, {})
  }

  reject(jobId: string, reason: string): Observable<ProcessingJobResponse> {
    return this.httpClient.post<ProcessingJobResponse>(`${this.endpointUrl}/
    ${jobId}/reject`, { reason })
  }

  revoke(jobId: string, reason: string): Observable<ProcessingJobResponse> {
    return this.httpClient.post<ProcessingJobResponse>(`${this.endpointUrl}/
    ${jobId}/revoke`, { reason })
  }
  
}
