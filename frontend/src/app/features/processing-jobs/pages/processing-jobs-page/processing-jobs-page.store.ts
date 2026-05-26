import { computed, inject, Injectable, signal } from '@angular/core'

import { SessionService } from '../../../../core/auth/session.service'
import { resolveHttpErrorMessage } from '../../../../shared/utils/http-error-message'
import { ProcessingJobResponse } from '../../models/processing-job.model'
import { ProcessingJobService } from '../../services/processing-job.service'

@Injectable()
export class ProcessingJobsPageStore {

  private readonly sessionService = inject(SessionService)
  private readonly processingJobService = inject(ProcessingJobService)

  readonly jobs = signal<ProcessingJobResponse[]>([])
  readonly errorMessage = signal<string | null>(null)
  readonly isLoading = signal(false)

  readonly currentUser = this.sessionService.currentUser
  readonly canFilterByUsername = computed(() =>
    this.currentUser()?.role === 'ADMINISTRATOR'
  )

  loadInitialJobs (): void {
    if (this.canFilterByUsername()) {
      this.loadJobs()
      return
    }

    this.loadMyJobs()
  }

  loadMyJobs (): void {
    const username = this.currentUser()?.username

    if (!username) {
      this.errorMessage.set('Authenticated user is missing.')
      return
    }

    this.loadJobs(username)
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

}
