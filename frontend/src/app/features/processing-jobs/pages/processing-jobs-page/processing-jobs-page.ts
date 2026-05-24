import { Component, computed, inject, OnInit, signal } from '@angular/core'

import { AppAlert } from '../../../../shared/components/app-alert/app-alert'
import { AppButton } from '../../../../shared/components/app-button/app-button'
import { AppPanel } from '../../../../shared/components/app-panel/app-panel'
import { AppTextInput } from '../../../../shared/components/app-text-input/app-text-input'
import { ConfirmationDialog } from '../../../../shared/components/confirmation-dialog/confirmation-dialog'
import { PageHeader } from '../../../../shared/components/page-header/page-header'
import { PageState } from '../../../../shared/components/page-state/page-state'
import { ProcessingJobDetailsPanel } from '../../components/processing-job-details-panel/processing-job-details-panel'
import { ProcessingJobStatusFilter } from '../../components/processing-job-status-filter/processing-job-status-filter'
import { ProcessingJobsList } from '../../components/processing-jobs-list/processing-jobs-list'
import { ProcessingJobsPageStore } from './processing-jobs-page.store'

type ProcessingConfirmationAction = 'reject' | 'revoke'

@Component({
  selector: 'app-processing-jobs-page',
  imports: [ AppAlert, AppButton, AppPanel, AppTextInput, ConfirmationDialog, PageHeader, PageState, ProcessingJobDetailsPanel, ProcessingJobsList, ProcessingJobStatusFilter ],
  providers: [ ProcessingJobsPageStore ],
  templateUrl: './processing-jobs-page.html'
})
export class ProcessingJobsPage implements OnInit {

  protected readonly store = inject(ProcessingJobsPageStore)
  protected readonly confirmationAction = signal<ProcessingConfirmationAction | null>(null)
  protected readonly isConfirmationOpen = computed(() => this.confirmationAction() !== null)
  protected readonly confirmationTitle = computed(() =>
    this.confirmationAction() === 'reject' ? 'Reject process' : 'Revoke approval'
  )
  protected readonly confirmationMessage = computed(() =>
    this.confirmationAction() === 'reject'
      ? 'Provide the reason for rejecting this processed result.'
      : 'Provide the reason for revoking this previous approval.'
  )
  protected readonly confirmationLabel = computed(() =>
    this.confirmationAction() === 'reject' ? 'Reject' : 'Revoke'
  )

  ngOnInit (): void {
    this.store.loadJobs()
  }

  protected rejectSelectedJob (): void {
    this.confirmationAction.set('reject')
  }

  protected revokeSelectedJob (): void {
    this.confirmationAction.set('revoke')
  }

  protected cancelConfirmation (): void {
    this.confirmationAction.set(null)
  }

  protected confirmAction (reason: string | null): void {
    const action = this.confirmationAction()

    if (!action || !reason) return

    if (action === 'reject') {
      this.store.rejectSelectedJob(reason)
    } else {
      this.store.revokeSelectedJob(reason)
    }

    this.confirmationAction.set(null)
  }

}
