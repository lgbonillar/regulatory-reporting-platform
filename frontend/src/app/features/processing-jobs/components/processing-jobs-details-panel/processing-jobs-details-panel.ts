import { DatePipe } from '@angular/common'
import { Component, input, output } from '@angular/core'

import { AppButton } from '../../../../shared/components/app-button/app-button'
import { FileDownloadLink } from '../../../../shared/components/file-download-link/file-download-link'
import { PageState } from '../../../../shared/components/page-state/page-state'
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge'
import { ProcessingJobResponse, ProcessingJobStatusHistoryResponse } from '../../models/processing-job.model'

@Component({
  selector: 'app-processing-job-details-panel',
  imports: [ AppButton, DatePipe, FileDownloadLink, PageState, StatusBadge ],
  template: `
    <aside class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm sm:p-6">
      @if (job(); as selectedJob) {
        <div class="border-b border-slate-200 pb-4">
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">
            Selected process
          </p>

          <h2 class="mt-1 break-words text-base font-semibold text-slate-950">
            {{ selectedJob.originalFilename }}
          </h2>

          <div class="mt-3 flex flex-wrap items-center gap-2">
            <app-status-badge [status]="selectedJob.jobStatus" />

            <span class="text-xs text-slate-500">
              {{ selectedJob.jobId }}
            </span>
          </div>

          <div class="mt-4 flex flex-wrap gap-2">
            @if (selectedJob.jobStatus === 'PENDING_EXECUTION') {
              <app-button
                variant="primary"
                [disabled]="isActionRunning()"
                [loading]="isActionRunning()"
                (click)="startRequested.emit()"
              >
                Start processing
              </app-button>
            }

            @if (selectedJob.jobStatus === 'AWAITING_APPROVAL') {
              <app-button
                variant="success"
                [disabled]="isActionRunning()"
                [loading]="isActionRunning()"
                (click)="approveRequested.emit()"
              >
                Approve
              </app-button>

              <app-button
                variant="warning"
                [disabled]="isActionRunning()"
                [loading]="isActionRunning()"
                (click)="rejectRequested.emit()"
              >
                Reject
              </app-button>
            }

            @if (selectedJob.jobStatus === 'APPROVED') {
              <app-button
                variant="secondary"
                [disabled]="isActionRunning()"
                [loading]="isActionRunning()"
                (click)="revokeRequested.emit()"
              >
                Revoke
              </app-button>
            }
          </div>
        </div>

        <dl class="mt-4 grid gap-4 text-sm">
          <div>
            <dt class="font-medium text-slate-500">Job ID</dt>
            <dd class="mt-1 break-all text-slate-900">{{ selectedJob.jobId }}</dd>
          </div>

          <div>
            <dt class="font-medium text-slate-500">File ID</dt>
            <dd class="mt-1 break-all text-slate-900">{{ selectedJob.fileId }}</dd>
          </div>

          <div>
            <dt class="font-medium text-slate-500">Filename</dt>
            <dd class="mt-1 break-words text-slate-900">
              <app-file-download-link
                [fileId]="selectedJob.fileId"
                [filename]="selectedJob.originalFilename"
                [fileStatus]="selectedJob.fileStatus"
              />
            </dd>
          </div>

          <div>
            <dt class="font-medium text-slate-500">Job Status</dt>
            <dd class="mt-1">
              <app-status-badge [status]="selectedJob.jobStatus" />
            </dd>
          </div>

          <div>
            <dt class="font-medium text-slate-500">File status</dt>
            <dd class="mt-1">
              <app-status-badge [status]="selectedJob.fileStatus" />
            </dd>
          </div>

          <div>
            <dt class="font-medium text-slate-500">Message</dt>
            <dd class="mt-1 text-slate-900">{{ selectedJob.message ?? 'No message' }}</dd>
          </div>

          <div>
            <dt class="font-medium text-slate-500">Uploaded by</dt>
            <dd class="mt-1 text-slate-900">{{ selectedJob.uploadedBy }}</dd>
          </div>

          @if (selectedJob.triggeredAt) {
            <div>
              <dt class="font-medium text-slate-500">Started by</dt>
              <dd class="mt-1 text-slate-900">{{ selectedJob.triggeredBy ?? 'Unknown user' }}</dd>
            </div>

            <div>
              <dt class="font-medium text-slate-500">Started at</dt>
              <dd class="mt-1 text-slate-900">{{ selectedJob.triggeredAt | date:'medium' }}</dd>
            </div>
          }

          @if (selectedJob.processingCompletedAt) {
            <div>
              <dt class="font-medium text-slate-500">Processing completed</dt>
              <dd class="mt-1 text-slate-900">{{ selectedJob.processingCompletedAt | date:'medium' }}</dd>
            </div>
          }

          @if (selectedJob.failureReason) {
            <div>
              <dt class="font-medium text-slate-500">Failure reason</dt>
              <dd class="mt-1 text-slate-900">{{ selectedJob.failureReason }}</dd>
            </div>
          }

          @if (selectedJob.approvedAt) {
            <div>
              <dt class="font-medium text-slate-500">Approved by</dt>
              <dd class="mt-1 text-slate-900">{{ selectedJob.approvedBy ?? 'Unknown user' }}</dd>
            </div>

            <div>
              <dt class="font-medium text-slate-500">Approved at</dt>
              <dd class="mt-1 text-slate-900">{{ selectedJob.approvedAt | date:'medium' }}</dd>
            </div>
          }

          @if (selectedJob.rejectedAt) {
            <div>
              <dt class="font-medium text-slate-500">Rejected by</dt>
              <dd class="mt-1 text-slate-900">{{ selectedJob.rejectedBy ?? 'Unknown user' }}</dd>
            </div>

            <div>
              <dt class="font-medium text-slate-500">Rejected at</dt>
              <dd class="mt-1 text-slate-900">{{ selectedJob.rejectedAt | date:'medium' }}</dd>
            </div>

            <div>
              <dt class="font-medium text-slate-500">Rejection reason</dt>
              <dd class="mt-1 text-slate-900">{{ selectedJob.rejectionReason }}</dd>
            </div>
          }

          @if (selectedJob.revokedAt) {
            <div>
              <dt class="font-medium text-slate-500">Revoked by</dt>
              <dd class="mt-1 text-slate-900">{{ selectedJob.revokedBy ?? 'Unknown user' }}</dd>
            </div>

            <div>
              <dt class="font-medium text-slate-500">Revoked at</dt>
              <dd class="mt-1 text-slate-900">{{ selectedJob.revokedAt | date: 'medium' }}</dd>
            </div>

            <div>
              <dt class="font-medium text-slate-500">Revocation reason</dt>
              <dd class="mt-1 text-slate-900">{{ selectedJob.revocationReason }}</dd>
            </div>
          }

          <div>
            <dt class="font-medium text-slate-500">Created</dt>
            <dd class="mt-1 text-slate-900">{{ selectedJob.createdAt | date: 'medium' }}</dd>
          </div>

          <div>
            <dt class="font-medium text-slate-500">Updated</dt>
            <dd class="mt-1 text-slate-900">
              @if (selectedJob.updatedAt) {
                {{ selectedJob.updatedAt | date: 'medium' }}
              } @else {
                Not updated yet
              }
            </dd>
          </div>
        </dl>

        <section class="mt-6 border-t border-slate-200 pt-4">
          <div class="flex items-center justify-between gap-3">
            <h3 class="text-sm font-semibold text-slate-950">History</h3>
          </div>

          @if (isHistoryLoading()) {

            <app-page-state
              type="loading"
              title="Loading history"
              message="Please wait while the status history is loaded."
            />

          } @else if (historyErrorMessage()) {

            <app-page-state
              type="error"
              title="History could not be loaded"
              [message]="historyErrorMessage()"
            />

          } @else if (history().length > 0) {

            <ol class="mt-4 space-y-4">
              @for (history of history(); track history.id) {
                <li class="border-l-2 border-slate-200 pl-4">
                  <div class="flex flex-wrap items-center gap-2">
                    @if (history.previousStatus) {
                      <app-status-badge [status]="history.previousStatus" />
                      <span class="text-xs text-slate-400">to</span>
                    }

                    <app-status-badge [status]="history.newStatus" />
                  </div>

                  <p class="mt-2 text-sm text-slate-900">
                    {{ history.reason ?? 'No reason provided' }}
                  </p>

                  <p class="mt-1 text-xs text-slate-500">
                    {{ getTransitionActorLabel(history) }} · {{ history.createdAt | date: 'medium' }}
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

      } @else {
        <app-page-state
          type="empty"
          title="No process selected"
          message="Select a processing job to view its details."
        />
      }
    </aside>
  `
})
export class ProcessingJobDetailsPanel {
  readonly job = input<ProcessingJobResponse | null>(null)
  readonly history = input<ProcessingJobStatusHistoryResponse[]>([])
  readonly isHistoryLoading = input(false)
  readonly historyErrorMessage = input<string | null>(null)
  readonly isActionRunning = input(false)

  readonly startRequested = output<void>()
  readonly approveRequested = output<void>()
  readonly rejectRequested = output<void>()
  readonly revokeRequested = output<void>()

  protected getTransitionActorLabel (history: ProcessingJobStatusHistoryResponse): string {
    if (history.transitionSource === 'SYSTEM') {
      return 'System'
    }

    return history.transitionedBy ?? 'Unknown user'
  }
}
