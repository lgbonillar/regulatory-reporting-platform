import { DatePipe } from '@angular/common'
import { Component, input, output } from '@angular/core'
import { RouterLink } from '@angular/router'
import { TableModule } from 'primeng/table'

import { AppButton } from '../../../../shared/components/app-button/app-button'
import { CopyableCode } from '../../../../shared/components/copyable-code/copyable-code'
import { FileDownloadLink } from '../../../../shared/components/file-download-link/file-download-link'
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge'
import { ProcessingJobResponse } from '../../models/processing-job.model'

@Component({
  selector: 'app-processing-jobs-list',
  host: {
    class: 'block h-full min-h-0'
  },
  imports: [ AppButton, CopyableCode, DatePipe, FileDownloadLink, RouterLink, StatusBadge, TableModule ],
  template: `
    <div class="hidden h-full min-h-0 lg:block">
      <p-table
        class="h-full! text-sm!"
        [value]="jobs()"
        [scrollable]="true"
        scrollHeight="flex"
        dataKey="jobId"
      >
        <ng-template #header>
          <tr>
            <th>File</th>
            <th>User</th>
            <th>Status</th>
            <th>Created</th>
            <th class="text-right">Action</th>
          </tr>
        </ng-template>

        <ng-template #body let-job>
          <tr>
            <td>
              <app-file-download-link
                [fileId]="job.fileId"
                [filename]="job.originalFilename"
                [fileStatus]="job.fileStatus"
              />

              <div class="mt-1">
                <app-copyable-code [value]="job.jobId" [ariaLabel]="'Copy job ID'" />
              </div>
            </td>

            <td class="text-slate-600">{{ job.uploadedBy }}</td>

            <td>
              <app-status-badge [status]="job.jobStatus" />
            </td>

            <td class="text-slate-600">
              {{ job.createdAt | date: 'medium' }}
            </td>

            <td class="text-right">
              <a
                [routerLink]="['/processing-jobs', job.jobId]"
                class="inline-flex cursor-pointer items-center gap-1 text-sm font-medium text-
slate-700 underline decoration-slate-300 underline-offset-4 hover:text-slate-950 hover:decoration-
slate-500"
              >
                Details
                <i class="fa-solid fa-arrow-right text-xs" aria-hidden="true"></i>
              </a>
            </td>
          </tr>
        </ng-template>
      </p-table>
    </div>

    <div class="h-full min-h-0 divide-y divide-slate-200 overflow-auto lg:hidden">
      @for (job of jobs(); track job.jobId) {
        <article class="flex flex-col gap-3 px-4 py-4 transition hover:bg-slate-50">
          <div>
            <app-file-download-link
              [fileId]="job.fileId"
              [filename]="job.originalFilename"
              [fileStatus]="job.fileStatus"
            />
            <div class="mt-1">
              <app-copyable-code [value]="job.jobId" [ariaLabel]="'Copy job ID'" />
            </div>
          </div>

          <div class="flex flex-wrap items-center gap-2">
            <app-status-badge [status]="job.jobStatus" />
            <span class="text-sm text-slate-500">{{ job.uploadedBy }}</span>
          </div>

          <div class="flex items-center justify-between gap-3">
            <p class="text-sm text-slate-500">{{ job.createdAt | date: 'mediumDate' }}</p>

            <app-button variant="secondary" (click)="jobSelected.emit(job)">
              Details
            </app-button>
          </div>
        </article>
      }
    </div>
  `
})
export class ProcessingJobsList {
  readonly jobs = input.required<ProcessingJobResponse[]>()
  readonly jobSelected = output<ProcessingJobResponse>()
}
