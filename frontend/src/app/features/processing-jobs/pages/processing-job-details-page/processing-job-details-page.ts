import { Component, inject, OnInit } from '@angular/core'
import { RouterLink } from '@angular/router'

import { AppAlert } from '../../../../shared/components/app-alert/app-alert'
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

  ngOnInit (): void {
    this.store.loadJob()
  }
}
