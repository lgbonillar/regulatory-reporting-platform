import { computed, inject, Injectable, signal } from '@angular/core'

import { ProcessingJobStatus } from '../../../../core/regulatory.model'
import { resolveHttpErrorMessage } from '../../../../shared/utils/http-error-message'
import { ProcessingJobResponse, ProcessingJobStatusHistoryResponse } from '../../models/processing-job.model'
import { ProcessingJobService } from '../../services/processing-job.service'

const CURRENT_USERNAME = 'analyst01'
const FIRST_JOB_INDEX = 0

@Injectable()
export class ProcessingJobsPageStore {

  private readonly processingJobService = inject(ProcessingJobService)

  private readonly currentUsername = CURRENT_USERNAME
  private readonly jobs = signal<ProcessingJobResponse[]>([])

  readonly selectedJob = signal<ProcessingJobResponse | null>(null)
  readonly selectedJobHistory = signal<ProcessingJobStatusHistoryResponse[]>([])
  readonly selectedStatuses = signal<Set<ProcessingJobStatus>>(new Set())
  readonly isStatusFilterOpen = signal(false)
  readonly errorMessage = signal<string | null>(null)
  readonly historyErrorMessage = signal<string | null>(null)
  readonly isLoading = signal(false)
  readonly isHistoryLoading = signal(false)
  readonly isActionRunning = signal(false)
  readonly filterUsername = signal('')

  readonly selectedStatusCount = computed(() => this.selectedStatuses().size)

  readonly statusOptions: readonly ProcessingJobStatus[] = [
    'PENDING_EXECUTION',
    'PROCESSING',
    'PROCESSING_FAILED',
    'AWAITING_APPROVAL',
    'APPROVED',
    'REJECTED',
    'REVOKED'
  ]

  readonly filteredJobs = computed(() => {
    const selectedStatuses = this.selectedStatuses()

    if (selectedStatuses.size === FIRST_JOB_INDEX) {
      return this.jobs()
    }

    return this.jobs().filter((job) => selectedStatuses.has(job.jobStatus))
  })

  loadMyJobs (): void {
    this.filterUsername.set(this.currentUsername)
    this.loadJobs(this.currentUsername)
  }

  loadJobs (username?: string): void {
    this.isLoading.set(true)
    this.errorMessage.set(null)

    const request = username
      ? this.processingJobService.listProcessingJobsByUsername(username)
      : this.processingJobService.listProcessingJobs()

    request.subscribe({
      next: (jobs) => {
        this.jobs.set(jobs)
        this.setSelectedJob(jobs.at(FIRST_JOB_INDEX) ?? null)
      },
      error: (error: unknown) => {
        this.errorMessage.set(resolveHttpErrorMessage(error))
        this.isLoading.set(false)
      },
      complete: () => {
        this.isLoading.set(false)
      }
    })
  }

  applyUsernameFilter (): void {
    const username = this.filterUsername().trim()

    if (!username) {
      this.loadJobs()
      return
    }

    this.loadJobs(username)
  }

  clearFilter (): void {
    this.filterUsername.set('')
    this.loadJobs()
  }

  toggleStatusFilterPanel (): void {
    this.isStatusFilterOpen.update((isOpen) => !isOpen)
  }

  closeStatusFilterPanel (): void {
    this.isStatusFilterOpen.set(false)
  }

  toggleStatusFilter (status: ProcessingJobStatus, checked: boolean): void {
    const updatedStatuses = new Set(this.selectedStatuses())

    if (checked) {
      updatedStatuses.add(status)
    } else {
      updatedStatuses.delete(status)
    }

    this.selectedStatuses.set(updatedStatuses)

    const visibleJobs = this.filteredJobs()
    const currentJob = this.selectedJob()
    const currentJobRemainsVisible = currentJob
      ? visibleJobs.some((job) => job.jobId === currentJob.jobId)
      : false

    if (!currentJobRemainsVisible) {
      this.setSelectedJob(visibleJobs.at(FIRST_JOB_INDEX) ?? null)
    }
  }

  clearStatusFilters (): void {
    this.selectedStatuses.set(new Set())
    this.setSelectedJob(this.jobs().at(FIRST_JOB_INDEX) ?? null)
  }

  selectJob (job: ProcessingJobResponse): void {
    this.setSelectedJob(job)
  }

  startSelectedJob (): void {
    const job = this.selectedJob()

    if (!job) {
      return
    }

    this.runJobAction(this.processingJobService.startProcessing(job.jobId))
  }

  approveSelectedJob (): void {
    const job = this.selectedJob()

    if (!job) {
      return
    }

    this.runJobAction(this.processingJobService.approve(job.jobId))
  }

  rejectSelectedJob (reason: string): void {
    const job = this.selectedJob()

    if (!job) {
      return
    }

    this.runJobAction(this.processingJobService.reject(job.jobId, reason))
  }

  revokeSelectedJob (reason: string): void {
    const job = this.selectedJob()

    if (!job) {
      return
    }

    this.runJobAction(this.processingJobService.revoke(job.jobId, reason))
  }

  private runJobAction (request: ReturnType<ProcessingJobService['startProcessing']>): void {
    this.isActionRunning.set(true)
    this.errorMessage.set(null)

    request.subscribe({
      next: (updatedJob) => {
        this.jobs.update((jobs) =>
          jobs.map((job) => job.jobId === updatedJob.jobId ? updatedJob : job)
        )
        this.setSelectedJob(updatedJob)
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

  private setSelectedJob (job: ProcessingJobResponse | null): void {
    this.selectedJob.set(job)

    if (!job) {
      this.selectedJobHistory.set([])
      this.historyErrorMessage.set(null)
      return
    }

    this.loadSelectedJobHistory(job.jobId)
  }

  private loadSelectedJobHistory (jobId: string): void {
    this.isHistoryLoading.set(true)
    this.historyErrorMessage.set(null)

    this.processingJobService.getProcessingJobHistory(jobId).subscribe({
      next: (history) => {
        this.selectedJobHistory.set(history)
      },
      error: (error: unknown) => {
        this.selectedJobHistory.set([])
        this.historyErrorMessage.set(resolveHttpErrorMessage(error))
        this.isHistoryLoading.set(false)
      },
      complete: () => {
        this.isHistoryLoading.set(false)
      }
    })
  }

}
