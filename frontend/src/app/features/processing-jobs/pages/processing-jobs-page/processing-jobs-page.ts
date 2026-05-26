import { Component, inject, OnInit } from '@angular/core'
import { Router } from '@angular/router'

import { AppAlert } from '../../../../shared/components/app-alert/app-alert'
import { AppBreadcrumb } from '../../../../shared/components/app-breadcrumb/app-breadcrumb'
import { AppPanel } from '../../../../shared/components/app-panel/app-panel'
import { PageHeader } from '../../../../shared/components/page-header/page-header'
import { PageState } from '../../../../shared/components/page-state/page-state'
import { TableExportMenu } from '../../../../shared/components/table-export-menu/table-export-menu'
import { ProcessingJobsList } from '../../components/processing-jobs-list/processing-jobs-list'
import { ProcessingJobResponse } from '../../models/processing-job.model'
import { ProcessingJobsPageStore } from './processing-jobs-page.store'

@Component({
  selector: 'app-processing-jobs-page',
  host: {
    class: 'block h-full min-h-0'
  },
  imports: [ AppAlert, AppBreadcrumb, AppPanel, PageHeader, PageState, ProcessingJobsList, TableExportMenu ],
  providers: [ ProcessingJobsPageStore ],
  templateUrl: './processing-jobs-page.html'
})
export class ProcessingJobsPage implements OnInit {

  protected readonly store = inject(ProcessingJobsPageStore)
  private readonly router = inject(Router)

  ngOnInit (): void {
    this.store.loadInitialJobs()
  }

  protected openJobDetails (job: ProcessingJobResponse): void {
    void this.router.navigate([ '/processing-jobs', job.jobId ])
  }

}
