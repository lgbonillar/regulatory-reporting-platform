import { DatePipe } from '@angular/common'
import { Component, input } from '@angular/core'

import { PageState } from '../../../../shared/components/page-state/page-state'
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge'
import { ProcessingJobStatusHistoryResponse } from '../../models/processing-job.model'

@Component({
  selector: 'app-processing-job-history-list',
  imports: [ DatePipe, PageState, StatusBadge ],
  template: `
    <section class="mt-6 border-t border-slate-200 pt-4">
      <div class="flex items-center justify-between gap-3">
        <h3 class="text-sm font-semibold text-slate-950">History</h3>
      </div>

      @if (isLoading()) {

        <app-page-state
          type="loading"
          title="Loading history"
          message="Please wait while the status history is loaded."
        />

      } @else if (errorMessage()) {

        <app-page-state
          type="error"
          title="History could not be loaded"
          [message]="errorMessage()"
        />

      } @else if (history().length > 0) {

        <ol class="mt-4 space-y-4">
          @for (historyItem of history(); track historyItem.id) {

            <li class="border-l-2 border-slate-200 pl-4">
              <div class="flex flex-wrap items-center gap-2">
                @if (historyItem.previousStatus) {
                  <app-status-badge [status]="historyItem.previousStatus" />
                  <span class="text-xs text-slate-400">to</span>
                }

                <app-status-badge [status]="historyItem.newStatus" />
              </div>

              <p class="mt-2 text-sm text-slate-900">
                {{ historyItem.reason ?? 'No reason provided' }}
              </p>

              <p class="mt-1 text-xs text-slate-500">
                {{ getTransitionActorLabel(historyItem) }} · {{ historyItem.createdAt | date: 'medium' }}
              </p>
            </li>

          }
        </ol>

      } @else {

        <app-page-state
          type="empty"
          title="No status history"
          message="This job does not have status transitions yet."
        />

      }
    </section>
  `
})
export class ProcessingJobHistoryList {

  readonly history = input<ProcessingJobStatusHistoryResponse[]>([])
  readonly isLoading = input(false)
  readonly errorMessage = input<string | null>(null)

  protected getTransitionActorLabel (history: ProcessingJobStatusHistoryResponse): string {
    if (history.transitionSource === 'SYSTEM') {
      return 'System'
    }

    return history.transitionedBy ?? 'Unknown user'
  }

}
