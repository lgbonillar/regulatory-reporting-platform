import { computed, inject, Injectable, signal } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { Observable } from 'rxjs'

import { resolveHttpErrorMessage } from '../../../../shared/utils/http-error-message'
import { ProcessingJobResponse, ProcessingJobStatusHistoryResponse } from '../../models/processing-job.model'
import { ProcessingJobService } from '../../services/processing-job.service'

@Injectable()
export class ProcessingJobDetailsPageStore {
  private readonly route = inject(ActivatedRoute)
  private readonly processingJobService = inject(ProcessingJobService)

  readonly job = signal<ProcessingJobResponse | null>(null)
  readonly history = signal<ProcessingJobStatusHistoryResponse[]>([])
  readonly errorMessage = signal<string | null>(null)
  readonly historyErrorMessage = signal<string | null>(null)
  readonly isLoading = signal(false)
  readonly isHistoryLoading = signal(false)
  readonly isActionRunning = signal(false)

  readonly jobId = computed(() => this.route.snapshot.paramMap.get('jobId'))

  loadJob (): void {
    const jobId = this.jobId()

    if (!jobId) {
      this.errorMessage.set('Processing job ID is missing.')
      return
    }

    this.isLoading.set(true)
    this.errorMessage.set(null)

    this.processingJobService.getProcessingJob(jobId).subscribe({
      next: (job) => {
        this.job.set(job)
        this.loadHistory(job.jobId)
      },
      error: (error: unknown) => {
        this.errorMessage.set(resolveHttpErrorMessage(error))
      },
      complete: () => {
        this.isLoading.set(false)
      }
    })
  }

  startSelectedJob (): void {
    const selectedJob = this.job()

    if (!selectedJob) {
      return
    }

    this.runJobAction(this.processingJobService.startProcessing(selectedJob.jobId))
  }

  approveSelectedJob (): void {
    const selectedJob = this.job()

    if (!selectedJob) {
      return
    }

    this.runJobAction(this.processingJobService.approve(selectedJob.jobId))
  }

  rejectSelectedJob (reason: string): void {
    const selectedJob = this.job()

    if (!selectedJob) {
      return
    }

    this.runJobAction(this.processingJobService.reject(selectedJob.jobId, reason))
  }

  revokeSelectedJob (reason: string): void {
    const selectedJob = this.job()

    if (!selectedJob) {
      return
    }

    this.runJobAction(this.processingJobService.revoke(selectedJob.jobId, reason))
  }

  private runJobAction (request: Observable<ProcessingJobResponse>): void {
    this.isActionRunning.set(true)
    this.errorMessage.set(null)

    request.subscribe({
      next: (updatedJob) => {
        this.job.set(updatedJob)
        this.loadHistory(updatedJob.jobId)
      },
      error: (error: unknown) => {
        this.errorMessage.set(resolveHttpErrorMessage(error))
        this.isActionRunning.set(false)
      },
      complete: () => {
        this.isActionRunning.set(false)
      }
    })
  }

  private loadHistory (jobId: string): void {
    this.isHistoryLoading.set(true)
    this.historyErrorMessage.set(null)

    this.processingJobService.getProcessingJobHistory(jobId).subscribe({
      next: (history) => {
        this.history.set(history)
      },
      error: (error: unknown) => {
        this.history.set([])
        this.historyErrorMessage.set(resolveHttpErrorMessage(error))
      },
      complete: () => {
        this.isHistoryLoading.set(false)
      }
    })
  }
}
