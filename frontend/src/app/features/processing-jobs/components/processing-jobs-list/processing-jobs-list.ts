import { DatePipe } from '@angular/common'
import { Component, input, output } from '@angular/core'

import { AppButton } from '../../../../shared/components/app-button/app-button'
import { CopyableCode } from '../../../../shared/components/copyable-code/copyable-code'
import { FileDownloadLink } from '../../../../shared/components/file-download-link/file-download-link'
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge'
import { ProcessingJobResponse } from '../../models/processing-job.model'

@Component({
  selector: 'app-processing-jobs-list',
  imports: [ AppButton, CopyableCode, DatePipe, FileDownloadLink, StatusBadge ],
  template: `
    <div class="hidden overflow-x-auto lg:block">
      <table class="min-w-full divide-y divide-slate-200 text-left text-sm">
        <thead class="bg-slate-50 text-xs font-semibold uppercase text-slate-500">
          <tr>
            <th class="px-6 py-3">File</th>
            <th class="px-6 py-3">User</th>
            <th class="px-6 py-3">Status</th>
            <th class="px-6 py-3">Created</th>
            <th class="px-6 py-3 text-right">Action</th>
          </tr>
        </thead>

        <tbody class="divide-y divide-slate-200">
          @for (job of jobs(); track job.jobId) {
            <tr
              class="align-middle transition"
              [class.bg-sky-50]="selectedJobId() === job.jobId"
              [class.ring-1]="selectedJobId() === job.jobId"
              [class.ring-inset]="selectedJobId() === job.jobId"
              [class.ring-sky-200]="selectedJobId() === job.jobId"
            >
              <td class="px-6 py-4">
                <app-file-download-link
                  [fileId]="job.fileId"
                  [filename]="job.originalFilename"
                  [fileStatus]="job.fileStatus"
                />
                <div class="mt-1">
                  <app-copyable-code [value]="job.jobId" [ariaLabel]="'Copy job ID'" />
                </div>
              </td>

              <td class="px-6 py-4 text-slate-600">{{ job.uploadedBy }}</td>

              <td class="px-6 py-4">
                <app-status-badge [status]="job.jobStatus" />
              </td>

              <td class="px-6 py-4 text-slate-600">
                {{ job.createdAt | date: 'medium' }}
              </td>

              <td class="px-6 py-4 text-right">
                <app-button variant="secondary" (click)="jobSelected.emit(job)">
                  {{ selectedJobId() === job.jobId ? 'Selected' : 'Details' }}
                </app-button>
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>

    <div class="divide-y divide-slate-200 lg:hidden">
      @for (job of jobs(); track job.jobId) {
        <article
          class="flex flex-col gap-3 px-4 py-4 transition"
          [class.bg-sky-50]="selectedJobId() === job.jobId"
        >
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
  readonly selectedJobId = input<string | null>(null)
  readonly jobSelected = output<ProcessingJobResponse>()
}
