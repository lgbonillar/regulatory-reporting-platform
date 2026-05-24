import { Component, input, output } from '@angular/core'

import { ProcessingJobStatus } from '../../../../core/regulatory.model'
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge'

const FIRST_FILE_INDEX = 0

@Component({
  selector: 'app-processing-job-status-filter',
  imports: [ StatusBadge ],
  template: `
    <div class="relative">
      <button
        class="inline-flex cursor-pointer items-center gap-2 rounded-md border border-slate-300 bg-white px-3 py-2
        text-sm font-medium text-slate-700 transition hover:bg-slate-50"
        type="button"
        [attr.aria-expanded]="isOpen()"
        aria-controls="job-status-filters"
        (click)="toggleRequested.emit()"
      >
        <svg
          class="size-4"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          aria-hidden="true"
        >
          <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3Z" />
        </svg>

        Filters

        @if (selectedCount() > 0) {
          <span class="rounded-full bg-slate-900 px-2 py-0.5 text-xs text-white">
            {{ selectedCount() }}
          </span>
        }
      </button>

      @if (isOpen()) {
        <div
          id="job-status-filters"
          class="absolute right-0 z-10 mt-2 w-72 rounded-lg border border-slate-200 bg-white p-3 shadow-lg"
        >
          <div class="mb-3 flex items-center justify-between">
            <p class="text-sm font-semibold text-slate-900">Status</p>

            <button
              class="cursor-pointer text-sm font-medium text-slate-600 hover:text-slate-900"
              type="button"
              (click)="clearRequested.emit()"
            >
              Clear
            </button>
          </div>

          <fieldset class="grid gap-2">
            <legend class="sr-only">Filter jobs by status</legend>

            @for (status of statuses(); track status) {
              <label class="flex cursor-pointer items-center gap-3 rounded-md px-2 py-2 hover:bg-slate-50">
                <input
                  class="size-4 cursor-pointer rounded border-slate-300"
                  type="checkbox"
                  [checked]="selectedStatuses().has(status)"
                  (change)="statusChanged.emit({ status, checked: $any($event.target).checked })"
                />

                <app-status-badge [status]="status" />
              </label>
            }
          </fieldset>
        </div>
      }
    </div>
  `
})
export class ProcessingJobStatusFilter {
  readonly statuses = input.required<readonly ProcessingJobStatus[]>()
  readonly selectedStatuses = input.required<Set<ProcessingJobStatus>>()
  readonly selectedCount = input(FIRST_FILE_INDEX)
  readonly isOpen = input(false)

  readonly toggleRequested = output<void>()
  readonly clearRequested = output<void>()
  readonly statusChanged = output<{ status: ProcessingJobStatus, checked: boolean }>()
}
