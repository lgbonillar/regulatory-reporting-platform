import { Component, inject, OnInit } from '@angular/core'

import { AppAlert } from '../../../../shared/components/app-alert/app-alert'
import { AppButton } from '../../../../shared/components/app-button/app-button'
import { AppPanel } from '../../../../shared/components/app-panel/app-panel'
import { AppTextInput } from '../../../../shared/components/app-text-input/app-text-input'
import { PageHeader } from '../../../../shared/components/page-header/page-header'
import { PageState } from '../../../../shared/components/page-state/page-state'
import { ProcessingJobDetailsPanel } from '../../components/processing-job-details-panel/processing-job-details-panel'
import { ProcessingJobStatusFilter } from '../../components/processing-job-status-filter/processing-job-status-filter'
import { ProcessingJobsList } from '../../components/processing-jobs-list/processing-jobs-list'
import { ProcessingJobsPageStore } from './processing-jobs-page.store'

@Component({
  selector: 'app-processing-jobs-page',
  imports: [ AppAlert, AppButton, AppPanel, AppTextInput, PageHeader, PageState, ProcessingJobDetailsPanel, ProcessingJobsList, ProcessingJobStatusFilter ],
  providers: [ ProcessingJobsPageStore ],
  templateUrl: './processing-jobs-page.html'
})
export class ProcessingJobsPage implements OnInit {

  protected readonly store = inject(ProcessingJobsPageStore)

  ngOnInit (): void {
    this.store.loadJobs()
  }

  protected rejectSelectedJob (): void {
    const reason = window.prompt('Reason for rejection')?.trim()

    if (!reason) return

    this.store.rejectSelectedJob(reason)
  }

  protected revokeSelectedJob (): void {
    const reason = window.prompt('Reason for revocation')?.trim()

    if (!reason) return

    this.store.revokeSelectedJob(reason)
  }

}
