import { Component, computed, input } from '@angular/core'
import { TagModule } from 'primeng/tag'

import { FileStatus, ProcessingJobStatus } from '../../../core/regulatory.model'

type StatusBadgeValue = FileStatus | ProcessingJobStatus
type TagSeverity = 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast'

@Component({
  selector: 'app-status-badge',
  imports: [ TagModule ],
  template: `
    <p-tag
      class="font-medium!"
      [value]="label()"
      [severity]="severity()"
      [rounded]="true"
    />
  `
})
export class StatusBadge {
  readonly status = input.required<StatusBadgeValue>()

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

  protected readonly severity = computed<TagSeverity>(() => {
    const status = this.status()

    const severityByStatus: Record<StatusBadgeValue, TagSeverity> = {
      STORED: 'success',
      MISSING: 'warn',
      FAILED: 'danger',
      DELETED: 'secondary',
      PENDING_EXECUTION: 'warn',
      PROCESSING: 'info',
      PROCESSING_FAILED: 'danger',
      AWAITING_APPROVAL: 'contrast',
      APPROVED: 'success',
      REJECTED: 'danger',
      REVOKED: 'secondary'
    }

    return severityByStatus[status]
  })
}
