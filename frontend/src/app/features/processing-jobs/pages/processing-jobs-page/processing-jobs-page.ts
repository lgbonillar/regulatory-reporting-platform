import { DatePipe } from '@angular/common'
import { HttpErrorResponse } from '@angular/common/http'
import { Component, computed, inject, OnInit, signal } from '@angular/core'
import { FormsModule } from '@angular/forms'

import { FileStatus, ProcessingJobStatus } from '../../../../core/regulatory.model'
import { FileDownloadLink } from '../../../../shared/components/file-download-link/file-download-link'
import { ProcessingJobResponse } from '../../models/processing-job.model'
import { ProcessingJobService } from '../../services/processing-job.service'

const CURRENT_USERNAME = 'analyst01'
const FIRST_FILE_INDEX = 0

@Component({
  selector: 'app-processing-jobs-page',
  imports: [ DatePipe, FormsModule, FileDownloadLink ],
  templateUrl: './processing-jobs-page.html'
})
export class ProcessingJobsPage implements OnInit {
  private readonly processingJobService = inject(ProcessingJobService)

  protected readonly jobs = signal<ProcessingJobResponse[]>([])
  protected readonly selectedJob = signal<ProcessingJobResponse | null>(null)
  protected readonly selectedStatuses = signal<Set<ProcessingJobStatus>>(new Set())
  protected readonly isStatusFilterOpen = signal(false)
  protected readonly errorMessage = signal<string | null>(null)
  protected readonly isLoading = signal(false)
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

    if (selectedStatuses.size === 0) {
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
      this.selectedJob.set(visibleJobs.at(FIRST_FILE_INDEX) ?? null)
    }
  }

  protected isStatusSelected (status: ProcessingJobStatus): boolean {
    return this.selectedStatuses().has(status)
  }

  protected clearStatusFilters (): void {
    this.selectedStatuses.set(new Set())
    this.selectedJob.set(this.jobs().at(FIRST_FILE_INDEX) ?? null)
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
    this.selectedJob.set(job)
  }

  protected getStatusClasses (status: ProcessingJobStatus): string {
    const classesByStatus: Record<ProcessingJobStatus, string> = {
      PENDING_EXECUTION: 'bg-amber-50 text-amber-800 ring-amber-200',
      PROCESSING: 'bg-sky-50 text-sky-800 ring-sky-200',
      PROCESSING_FAILED: 'bg-red-50 text-red-800 ring-red-200',
      AWAITING_APPROVAL: 'bg-violet-50 text-violet-800 ring-violet-200',
      APPROVED: 'bg-emerald-50 text-emerald-800 ring-emerald-200',
      REJECTED: 'bg-rose-50 text-rose-800 ring-rose-200',
      REVOKED: 'bg-slate-100 text-slate-700 ring-slate-300'
    }

    return classesByStatus[status]
  }

  protected getFileStatusClasses (status: FileStatus): string {
    const classesByStatus: Record<FileStatus, string> = {
      STORED: 'bg-emerald-50 text-emerald-700',
      MISSING: 'bg-amber-50 text-amber-700',
      FAILED: 'bg-red-50 text-red-700',
      DELETED: 'bg-slate-100 text-slate-600'
    }

    return classesByStatus[status]
  }

  protected getStatusLabel (status: ProcessingJobStatus): string {
    const labelsByStatus: Record<ProcessingJobStatus, string> = {
      PENDING_EXECUTION: 'Pending execution',
      PROCESSING: 'Processing',
      PROCESSING_FAILED: 'Processing failed',
      AWAITING_APPROVAL: 'Awaiting approval',
      APPROVED: 'Approved',
      REJECTED: 'Rejected',
      REVOKED: 'Revoked'
    }

    return labelsByStatus[status]
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
        this.selectedJob.set(jobs.at(FIRST_FILE_INDEX) ?? null)
      },
      error: (error: unknown) => {
        this.errorMessage.set(this.resolveErrorMessage(error))
      },
      complete: () => {
        this.isLoading.set(false)
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
