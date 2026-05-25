import { Component, inject, OnInit } from '@angular/core'
import { Router } from '@angular/router'

import { AppAlert } from '../../../../shared/components/app-alert/app-alert'
import { AppButton } from '../../../../shared/components/app-button/app-button'
import { AppPanel } from '../../../../shared/components/app-panel/app-panel'
import { AppTextInput } from '../../../../shared/components/app-text-input/app-text-input'
import { PageHeader } from '../../../../shared/components/page-header/page-header'
import { PageState } from '../../../../shared/components/page-state/page-state'
import { ProcessingJobStatusFilter } from '../../components/processing-job-status-filter/processing-job-status-filter'
import { ProcessingJobsList } from '../../components/processing-jobs-list/processing-jobs-list'
import { ProcessingJobResponse } from '../../models/processing-job.model'
import { ProcessingJobsPageStore } from './processing-jobs-page.store'

@Component({
  selector: 'app-processing-jobs-page',
  host: {
    class: 'block h-full min-h-0'
  },
  imports: [ AppAlert, AppButton, AppPanel, AppTextInput, PageHeader, PageState, ProcessingJobsList, ProcessingJobStatusFilter ],
  providers: [ ProcessingJobsPageStore ],
  templateUrl: './processing-jobs-page.html'
})
export class ProcessingJobsPage implements OnInit {

  protected readonly store = inject(ProcessingJobsPageStore)
  private readonly router = inject(Router)

  ngOnInit (): void {
    this.store.loadJobs()
  }

  protected openJobDetails (job: ProcessingJobResponse): void {
    void this.router.navigate([ '/processes', job.jobId ])
  }

}
