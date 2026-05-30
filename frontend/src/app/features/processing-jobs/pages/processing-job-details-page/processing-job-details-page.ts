import { DatePipe } from '@angular/common'
import { Component, computed, inject, OnInit, signal } from '@angular/core'

import { AppAlert } from '../../../../shared/components/app-alert/app-alert'
import { AppBreadcrumb, AppBreadcrumbItem } from '../../../../shared/components/app-breadcrumb/app-breadcrumb'
import { ConfirmationDialog } from '../../../../shared/components/confirmation-dialog/confirmation-dialog'
import { CopyableCode } from '../../../../shared/components/copyable-code/copyable-code'
import { FileDownloadLink } from '../../../../shared/components/file-download-link/file-download-link'
import { PageState } from '../../../../shared/components/page-state/page-state'
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge'
import { ButtonModule } from 'primeng/button'
import { Popover } from 'primeng/popover'
import { TooltipModule } from 'primeng/tooltip'
import { ProcessingJobFindingsList } from '../../components/processing-job-findings-list/processing-job-findings-list'
import { ProcessingJobHistoryList } from '../../components/processing-job-history-list/processing-job-history-list'
import { ProcessingJobDetailsPageStore } from './processing-job-details-page.store'

@Component({
  selector: 'app-processing-job-details-page',
  host: {
    class: 'block h-full min-h-0'
  },
  imports: [
    AppAlert,
    AppBreadcrumb,
    ButtonModule,
    ConfirmationDialog,
    CopyableCode,
    DatePipe,
    FileDownloadLink,
    PageState,
    Popover,
    ProcessingJobFindingsList,
    ProcessingJobHistoryList,
    StatusBadge,
    TooltipModule
  ],
  providers: [ ProcessingJobDetailsPageStore ],
  templateUrl: './processing-job-details-page.html'
})
export class ProcessingJobDetailsPage implements OnInit {
  protected readonly store = inject(ProcessingJobDetailsPageStore)
  protected readonly pendingConfirmation = signal<'reject' | 'revoke' | null>(null)
  protected readonly nerdInfoPopover = signal<Popover | null>(null)
  protected readonly breadcrumbItems = computed<readonly AppBreadcrumbItem[]>(() => [
    {
      label: 'processing-jobs',
      route: '/processing-jobs'
    },
    {
      label: this.store.job()?.originalFilename ?? 'details'
    }
  ])

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

  protected openNerdInfo (event: MouseEvent, popover: Popover): void {
    popover.show(event)
    this.nerdInfoPopover.set(popover)
  }

  protected closeNerdInfo (): void {
    const popover = this.nerdInfoPopover()
    if (popover) {
      popover.hide()
      this.nerdInfoPopover.set(null)
    }
  }
}
