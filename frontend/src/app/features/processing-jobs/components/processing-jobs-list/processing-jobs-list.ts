import { DatePipe } from '@angular/common'
import { Component, computed, input } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { RouterLink } from '@angular/router'
import { ButtonModule } from 'primeng/button'
import { DatePickerModule } from 'primeng/datepicker'
import { MultiSelectModule } from 'primeng/multiselect'
import { TableModule } from 'primeng/table'
import { TooltipModule } from 'primeng/tooltip'

import { CopyableCode } from '../../../../shared/components/copyable-code/copyable-code'
import { FileDownloadLink } from '../../../../shared/components/file-download-link/file-download-link'
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge'
import { ProcessingJobResponse } from '../../models/processing-job.model'

@Component({
  selector: 'app-processing-jobs-list',
  host: {
    class: 'block h-full min-h-0'
  },
  imports: [ ButtonModule, CopyableCode, DatePipe, DatePickerModule, FileDownloadLink, FormsModule, MultiSelectModule, RouterLink, StatusBadge, TableModule, TooltipModule ],
  template: `
    <div class="hidden h-full min-h-0 lg:block">
      <p-table
        class="h-full! text-sm!"
        [value]="tableJobs()"
        [scrollable]="true"
        scrollHeight="flex"
        dataKey="jobId"
      >
        <ng-template #header>
          <tr>
            <th>
              <div class="flex cursor-pointer items-center justify-between gap-2">
                <span>File</span>

                <p-columnFilter
                  field="originalFilenameFilter"
                  display="menu"
                  matchMode="contains"
                  [showMatchModes]="false"
                  [showOperator]="false"
                  [showAddButton]="false"
                  [showApplyButton]="false"
                >
                  <ng-template #filter let-value let-filter="filterCallback">
                    <input
                      class="w-64 rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 outline-none transition focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
                      type="text"
                      placeholder="Filter by file name"
                      [ngModel]="value"
                      (ngModelChange)="filter($event ? $event.toLowerCase() : null)"
                    />
                  </ng-template>
                </p-columnFilter>
              </div>
            </th>
            <th>
              <div class="flex cursor-pointer items-center justify-between gap-2">
                <span>User</span>

                <p-columnFilter
                  #userFilter
                  field="uploadedBy"
                  matchMode="in"
                  display="menu"
                  [showMatchModes]="false"
                  [showOperator]="false"
                  [showAddButton]="false"
                  [showApplyButton]="false"
                >
                  <ng-template #filter let-value let-filter="filterCallback">
                    <p-multiselect
                      class="w-64"
                      optionLabel="label"
                      optionValue="value"
                      placeholder="Select users"
                      [options]="userOptions()"
                      [ngModel]="value"
                      (ngModelChange)="filter($event?.length ? $event : null)"
                    />
                  </ng-template>
                </p-columnFilter>
              </div>
            </th>
            <th>
              <div class="flex cursor-pointer items-center justify-between gap-2">
                <span>Status</span>

                <p-columnFilter
                  #statusFilter
                  field="jobStatus"
                  matchMode="in"
                  display="menu"
                  [showMatchModes]="false"
                  [showOperator]="false"
                  [showAddButton]="false"
                  [showApplyButton]="false"
                >
                  <ng-template #filter let-value let-filter="filterCallback">
                    <p-multiselect
                      class="w-64"
                      optionLabel="label"
                      optionValue="value"
                      placeholder="Select statuses"
                      [options]="statusOptions()"
                      [ngModel]="value"
                      (ngModelChange)="filter($event?.length ? $event : null)"
                    >
                      <ng-template #item let-option>
                        <app-status-badge [status]="option.value" />
                      </ng-template>

                      <ng-template #selectedItems let-value>
                        @if (value?.length) {
                          <span class="text-sm text-slate-700">
                            {{ value.length }} selected
                          </span>
                        } @else {
                          <span class="text-sm text-slate-400">Select statuses</span>
                        }
                      </ng-template>
                    </p-multiselect>
                  </ng-template>
                </p-columnFilter>
              </div>
            </th>
            <th>
              <div class="flex cursor-pointer items-center justify-between gap-2">
                <span>Created</span>

                <p-columnFilter
                  field="createdAtDate"
                  type="date"
                  display="menu"
                  matchMode="between"
                  [showMatchModes]="false"
                  [showOperator]="false"
                  [showAddButton]="false"
                  [showApplyButton]="false"
                >
                  <ng-template #filter let-value let-filter="filterCallback">
                    <p-datepicker
                      class="w-72"
                      selectionMode="range"
                      placeholder="Select date range"
                      [ngModel]="value"
                      (ngModelChange)="filter($event)"
                      [showIcon]="true"
                    />
                  </ng-template>
                </p-columnFilter>
              </div>
            </th>
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
              <p-button
                styleClass="cursor-pointer"
                icon="fa-solid fa-list-check"
                severity="info"
                [outlined]="true"
                [routerLink]="['/processing-jobs', job.jobId]"
                pTooltip="View findings"
                tooltipPosition="top"
                showDelay="500"
                hideDelay="100"
                ariaLabel="View findings"
              />
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

            <p-button
              styleClass="cursor-pointer"
              icon="fa-solid fa-list-check"
              severity="info"
              [outlined]="true"
              [routerLink]="['/processing-jobs', job.jobId]"
              pTooltip="View findings"
              tooltipPosition="top"
              showDelay="500"
              hideDelay="100"
              ariaLabel="View findings"
            />
          </div>
        </article>
      }
    </div>
  `
})
export class ProcessingJobsList {
  readonly jobs = input.required<ProcessingJobResponse[]>()

  protected readonly userOptions = computed(() =>
    Array.from(new Set(this.jobs().map((job) => job.uploadedBy)))
      .sort()
      .map((user) => ({
        label: user,
        value: user
      }))
  )

  protected readonly statusOptions = computed(() =>
    Array.from(new Set(this.jobs().map((job) => job.jobStatus)))
      .sort()
      .map((status) => ({
        label: status,
        value: status
      }))
  )

  protected readonly tableJobs = computed(() =>
    this.jobs().map((job) => ({
      ...job,
      originalFilenameFilter: job.originalFilename.toLowerCase(),
      createdAtDate: this.toDateOnly(job.createdAt)
    }))
  )

  private toDateOnly (value: string): Date {
    const date = new Date(value)

    return new Date(date.getFullYear(), date.getMonth(), date.getDate())
  }
}
