import { HttpErrorResponse } from '@angular/common/http'
import { Component, computed, inject, OnInit, signal } from '@angular/core'

import { ProcessingJobStatus } from '../../../../core/regulatory.model'
import { AppAlert } from '../../../../shared/components/app-alert/app-alert'
import { AppButton } from '../../../../shared/components/app-button/app-button'
import { AppPanel } from '../../../../shared/components/app-panel/app-panel'
import { AppTextInput } from '../../../../shared/components/app-text-input/app-text-input'
import { PageHeader } from '../../../../shared/components/page-header/page-header'
import { PageState } from '../../../../shared/components/page-state/page-state'
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge'
import { ProcessingJobDetailsPanel } from '../../components/processing-job-details-panel/processing-job-details-panel'
import { ProcessingJobsList } from '../../components/processing-jobs-list/processing-jobs-list'
import { ProcessingJobResponse, ProcessingJobStatusHistoryResponse } from '../../models/processing-job.model'
import { ProcessingJobService } from '../../services/processing-job.service'

const CURRENT_USERNAME = 'analyst01'
const FIRST_FILE_INDEX = 0

@Component({
  selector: 'app-processing-jobs-page',
  imports: [ AppAlert, AppButton, AppPanel, AppTextInput, StatusBadge, PageHeader, PageState, ProcessingJobDetailsPanel, ProcessingJobsList ],
  templateUrl: './processing-jobs-page.html'
})
export class ProcessingJobsPage implements OnInit {

  private readonly processingJobService = inject(ProcessingJobService)

  protected readonly jobs = signal<ProcessingJobResponse[]>([])
  protected readonly selectedJob = signal<ProcessingJobResponse | null>(null)
  protected readonly selectedJobHistory = signal<ProcessingJobStatusHistoryResponse[]>([])
  protected readonly selectedStatuses = signal<Set<ProcessingJobStatus>>(new Set())
  protected readonly isStatusFilterOpen = signal(false)
  protected readonly errorMessage = signal<string | null>(null)
  protected readonly historyErrorMessage = signal<string | null>(null)
  protected readonly isLoading = signal(false)
  protected readonly isHistoryLoading = signal(false)
  protected readonly isActionRunning = signal(false)
  protected readonly filterUsername = signal('')
  protected readonly currentUsername = CURRENT_USERNAME
  protected readonly selectedStatusCount = computed(() => this.selectedStatuses().size)

  protected readonly statusOptions: readonly ProcessingJobStatus[] = [
    'PENDING_EXECUTION',
    'PROCESSING',
    'PROCESSING_FAILED',
    'AWAITING_APPROVAL',
    'APPROVED',
    'REJECTED',
    'REVOKED'
  ]

  protected readonly filteredJobs = computed(() => {
    const selectedStatuses = this.selectedStatuses()

    if (selectedStatuses.size === FIRST_FILE_INDEX) {
      return this.jobs()
    }

    return this.jobs().filter((job) => selectedStatuses.has(job.jobStatus))
  })

  ngOnInit (): void {
    this.loadAllJobs()
  }

  protected toggleStatusFilterPanel (): void {
    this.isStatusFilterOpen.update((isOpen) => !isOpen)
  }

  protected toggleStatusFilter (status: ProcessingJobStatus, checked: boolean): void {
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
      this.setSelectedJob(visibleJobs.at(FIRST_FILE_INDEX) ?? null)
    }
  }

  protected isStatusSelected (status: ProcessingJobStatus): boolean {
    return this.selectedStatuses().has(status)
  }

  protected clearStatusFilters (): void {
    this.selectedStatuses.set(new Set())
    this.setSelectedJob(this.jobs().at(FIRST_FILE_INDEX) ?? null)
  }

  protected onStatusFilterChange (event: Event, status: ProcessingJobStatus): void {
    const checkbox = event.target as HTMLInputElement
    this.toggleStatusFilter(status, checkbox.checked)
  }

  protected loadAllJobs (): void {
    this.loadJobs()
  }

  protected loadMyJobs (): void {
    this.filterUsername.set(this.currentUsername)
    this.loadJobs(this.currentUsername)
  }

  protected applyUsernameFilter (): void {
    const username = this.filterUsername().trim()

    if (!username) {
      this.loadJobs()
      return
    }

    this.loadJobs(username)
  }

  protected clearFilter (): void {
    this.filterUsername.set('')
    this.loadJobs()
  }

  protected selectJob (job: ProcessingJobResponse): void {
    this.setSelectedJob(job)
  }

  protected startSelectedJob (): void {
    const job = this.selectedJob()

    if (!job) {
      return
    }

    this.runJobAction(this.processingJobService.startProcessing(job.jobId))
  }

  protected approveSelectedJob (): void {
    const job = this.selectedJob()

    if (!job) {
      return
    }

    this.runJobAction(this.processingJobService.approve(job.jobId))
  }

  protected rejectSelectedJob (): void {
    const job = this.selectedJob()

    if (!job) {
      return
    }

    const reason = window.prompt('Reason for rejection')?.trim()

    if (!reason) {
      return
    }

    this.runJobAction(this.processingJobService.reject(job.jobId, reason))
  }

  protected revokeSelectedJob (): void {
    const job = this.selectedJob()

    if (!job) {
      return
    }

    const reason = window.prompt('Reason for revocation')?.trim()

    if (!reason) {
      return
    }

    this.runJobAction(this.processingJobService.revoke(job.jobId, reason))
  }

  protected getTransitionActorLabel (history: ProcessingJobStatusHistoryResponse): string {
    if (history.transitionSource === 'SYSTEM') {
      return 'System'
    }

    return history.transitionedBy ?? 'Unknown user'
  }

  private loadJobs (username?: string): void {
    this.isLoading.set(true)
    this.errorMessage.set(null)

    const request = username
      ? this.processingJobService.listProcessingJobsByUsername(username)
      : this.processingJobService.listProcessingJobs()

    request.subscribe({
      next: (jobs) => {
        this.jobs.set(jobs)
        this.setSelectedJob(jobs.at(FIRST_FILE_INDEX) ?? null)
      },
      error: (error: unknown) => {
        this.errorMessage.set(this.resolveErrorMessage(error))
        this.isLoading.set(false)
      },
      complete: () => {
        this.isLoading.set(false)
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
        this.historyErrorMessage.set(this.resolveErrorMessage(error))
        this.isHistoryLoading.set(false)
      },
      complete: () => {
        this.isHistoryLoading.set(false)
      }
    })
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
        this.errorMessage.set(this.resolveErrorMessage(error))
        this.isActionRunning.set(false)
      },
      complete: () => {
        this.isActionRunning.set(false)
      }
    })
  }

  private resolveErrorMessage (error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      return error.error?.message ?? 'Request failed. Please try again.'
    }

    return 'Unexpected error. Please try again.'
  }

}
