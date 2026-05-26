import { DatePipe } from '@angular/common'
import { Component, input, output } from '@angular/core'

import { AppButton } from '../../../../shared/components/app-button/app-button'
import { CopyableCode } from '../../../../shared/components/copyable-code/copyable-code'
import { FileDownloadLink } from '../../../../shared/components/file-download-link/file-download-link'
import { PageState } from '../../../../shared/components/page-state/page-state'
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge'
import { ProcessingJobResponse } from '../../models/processing-job.model'

@Component({
  selector: 'app-processing-job-details-panel',
  imports: [ AppButton, CopyableCode, DatePipe, FileDownloadLink, PageState, StatusBadge ],
  template: `
    <aside class="flex h-full min-h-0 flex-col overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
      @if (job(); as selectedJob) {
        <div class="shrink-0 border-b border-slate-200 bg-white p-4 sm:p-6">
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">
            Selected process
          </p>

          <h2 class="mt-1 text-base font-semibold text-slate-950">
            <app-file-download-link
              [fileId]="selectedJob.fileId"
              [filename]="selectedJob.originalFilename"
              [fileStatus]="selectedJob.fileStatus"
            />
          </h2>

          <div class="mt-3 flex flex-wrap items-center gap-2">
            <app-status-badge [status]="selectedJob.jobStatus" />
            <app-status-badge [status]="selectedJob.fileStatus" />
          </div>

          <div class="mt-4 flex flex-wrap gap-2">
            @if (canStartProcessing()) {
              <app-button
                variant="primary"
                [disabled]="isActionRunning()"
                [loading]="isActionRunning()"
                (click)="startRequested.emit()"
              >
                <span class="inline-flex items-center gap-2">
                  <i class="fa-solid fa-play" aria-hidden="true"></i>
                  Start processing
                </span>
              </app-button>
            }

            @if (canApprove()) {
              <app-button
                variant="success"
                [disabled]="isActionRunning()"
                [loading]="isActionRunning()"
                (click)="approveRequested.emit()"
              >
                Approve
              </app-button>
            }

            @if (canReject()) {
              <app-button
                variant="warning"
                [disabled]="isActionRunning()"
                [loading]="isActionRunning()"
                (click)="rejectRequested.emit()"
              >
                Reject
              </app-button>
            }

            @if (canRevoke()) {
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

        <dl class="grid min-h-0 flex-1 gap-4 overflow-auto p-4 text-sm sm:p-6">
          <div>
            <dt class="font-medium text-slate-500">Job ID</dt>
            <dd class="mt-1">
              <app-copyable-code [value]="selectedJob.jobId" [ariaLabel]="'Copy job ID'" />
            </dd>
          </div>

          <div>
            <dt class="font-medium text-slate-500">File ID</dt>
            <dd class="mt-1">
              <app-copyable-code [value]="selectedJob.fileId" [ariaLabel]="'Copy file ID'" />
            </dd>
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
      } @else {
        <div class="p-6">
          <app-page-state
            type="empty"
            title="No process selected"
            message="Select a processing job to view its details."
          />
        </div>
      }
    </aside>
  `
})
export class ProcessingJobDetailsPanel {
  readonly job = input<ProcessingJobResponse | null>(null)
  readonly isActionRunning = input(false)
  readonly canStartProcessing = input(false)
  readonly canApprove = input(false)
  readonly canReject = input(false)
  readonly canRevoke = input(false)

  readonly startRequested = output<void>()
  readonly approveRequested = output<void>()
  readonly rejectRequested = output<void>()
  readonly revokeRequested = output<void>()

}
