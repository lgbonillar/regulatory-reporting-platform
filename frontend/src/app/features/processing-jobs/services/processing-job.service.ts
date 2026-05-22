import { HttpClient } from '@angular/common/http'
import { inject, Injectable } from '@angular/core'
import { Observable } from 'rxjs'

import { environment } from '../../../../environments/environments.dev'
import { ProcessingJobResponse } from '../models/processing-job.model'

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
}
