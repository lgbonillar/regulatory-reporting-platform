import { Component, inject, OnInit, signal } from '@angular/core'
import { RouterLink } from '@angular/router'

import { AppAlert } from '../../../../shared/components/app-alert/app-alert'
import { ConfirmationDialog } from '../../../../shared/components/confirmation-dialog/confirmation-dialog'
import { PageHeader } from '../../../../shared/components/page-header/page-header'
import { PageState } from '../../../../shared/components/page-state/page-state'
import { ProcessingJobDetailsPanel } from '../../components/processing-job-details-panel/processing-job-details-panel'
import { ProcessingJobHistoryList } from '../../components/processing-job-history-list/processing-job-history-list'
import { ProcessingJobDetailsPageStore } from './processing-job-details-page.store'

@Component({
  selector: 'app-processing-job-details-page',
  host: {
    class: 'block h-full min-h-0'
  },
  imports: [
    AppAlert,
    ConfirmationDialog,
    PageHeader,
    PageState,
    ProcessingJobDetailsPanel,
    ProcessingJobHistoryList,
    RouterLink
  ],
  providers: [ ProcessingJobDetailsPageStore ],
  templateUrl: './processing-job-details-page.html'
})
export class ProcessingJobDetailsPage implements OnInit {
  protected readonly store = inject(ProcessingJobDetailsPageStore)
  protected readonly pendingConfirmation = signal<'reject' | 'revoke' | null>(null)

  ngOnInit (): void {
    this.store.loadJob()
  }

  protected requestReject (): void {
    this.pendingConfirmation.set('reject')
  }

  protected requestRevoke (): void {
    this.pendingConfirmation.set('revoke')
  }

  protected closeConfirmation (): void {
    this.pendingConfirmation.set(null)
  }

  protected confirmAction (reason: string | null): void {
    const pendingConfirmation = this.pendingConfirmation()
    const confirmationReason = reason?.trim()

    if (!pendingConfirmation || !confirmationReason) {
      return
    }

    if (pendingConfirmation === 'reject') {
      this.store.rejectSelectedJob(confirmationReason)
    } else {
      this.store.revokeSelectedJob(confirmationReason)
    }

    this.closeConfirmation()
  }
}
