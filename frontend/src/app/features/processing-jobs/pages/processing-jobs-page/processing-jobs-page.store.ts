import { computed, inject, Injectable, signal } from '@angular/core'

import { ProcessingJobStatus } from '../../../../core/regulatory.model'
import { resolveHttpErrorMessage } from '../../../../shared/utils/http-error-message'
import { ProcessingJobResponse } from '../../models/processing-job.model'
import { ProcessingJobService } from '../../services/processing-job.service'

const CURRENT_USERNAME = 'analyst01'
const EMPTY_SELECTION_COUNT = 0

@Injectable()
export class ProcessingJobsPageStore {

  private readonly processingJobService = inject(ProcessingJobService)

  private readonly currentUsername = CURRENT_USERNAME
  private readonly jobs = signal<ProcessingJobResponse[]>([])

  readonly selectedStatuses = signal<Set<ProcessingJobStatus>>(new Set())
  readonly isStatusFilterOpen = signal(false)
  readonly errorMessage = signal<string | null>(null)
  readonly isLoading = signal(false)
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

    if (selectedStatuses.size === EMPTY_SELECTION_COUNT) {
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
  }

  clearStatusFilters (): void {
    this.selectedStatuses.set(new Set())
  }

}
