import { Component, computed, input } from '@angular/core'

import { FileStatus, ProcessingJobStatus } from '../../../core/regulatory.model'

type StatusBadgeValue = FileStatus | ProcessingJobStatus
type StatusBadgeVariant = 'file' | 'job'

@Component({
  selector: 'app-status-badge',
  template: `
    <span
      class="inline-flex rounded-full px-2 py-1 text-xs font-medium ring-1 ring-inset"
      [class]="classes()"
    >
      {{ label() }}
    </span>
  `
})
export class StatusBadge {
  readonly status = input.required<StatusBadgeValue>()
  readonly variant = input.required<StatusBadgeVariant>()

  protected readonly label = computed(() => {
    const status = this.status()

    const labelsByStatus: Record<StatusBadgeValue, string> = {
      STORED: 'Stored',
      MISSING: 'Missing',
      FAILED: 'Failed',
      DELETED: 'Deleted',
      PENDING_EXECUTION: 'Pending execution',
      PROCESSING: 'Processing',
      PROCESSING_FAILED: 'Processing failed',
      AWAITING_APPROVAL: 'Awaiting approval',
      APPROVED: 'Approved',
      REJECTED: 'Rejected',
      REVOKED: 'Revoked'
    }

    return labelsByStatus[status]
  })

  protected readonly classes = computed(() => {
    const status = this.status()

    const classesByStatus: Record<StatusBadgeValue, string> = {
      STORED: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
      MISSING: 'bg-amber-50 text-amber-700 ring-amber-200',
      FAILED: 'bg-red-50 text-red-700 ring-red-200',
      DELETED: 'bg-slate-100 text-slate-600 ring-slate-300',
      PENDING_EXECUTION: 'bg-amber-50 text-amber-800 ring-amber-200',
      PROCESSING: 'bg-sky-50 text-sky-800 ring-sky-200',
      PROCESSING_FAILED: 'bg-red-50 text-red-800 ring-red-200',
      AWAITING_APPROVAL: 'bg-violet-50 text-violet-800 ring-violet-200',
      APPROVED: 'bg-emerald-50 text-emerald-800 ring-emerald-200',
      REJECTED: 'bg-rose-50 text-rose-800 ring-rose-200',
      REVOKED: 'bg-slate-100 text-slate-700 ring-slate-300'
    }

    return classesByStatus[status]
  })
}
