import { DatePipe } from '@angular/common'
import { Component, input } from '@angular/core'

import { PageState } from '../../../../shared/components/page-state/page-state'
import { ProcessingJobFindingResponse } from '../../models/processing-job.model'

@Component({
  selector: 'app-processing-job-findings-list',
  imports: [ DatePipe, PageState ],
  template: `
    <section class="flex h-full min-h-0 flex-col">
      <div class="min-h-0 flex-1 overflow-auto p-4 sm:p-6">
        @if (isLoading()) {

          <app-page-state
            type="loading"
            title="Loading findings"
            message="Please wait while the findings are loaded."
          />

        } @else if (errorMessage()) {

          <app-page-state
            type="error"
            title="Findings could not be loaded"
            [message]="errorMessage()"
          />

        } @else if (findings().length > 0) {

          <ol class="space-y-3">
            @for (finding of findings(); track finding.id) {

              <li class="rounded-lg border border-slate-200 bg-slate-50 p-4">
                <div class="flex flex-wrap items-center gap-2">
                  <span class="inline-flex items-center rounded px-2 py-0.5 text-xs font-medium" [class]="getSeverityClass(finding.severity)">
                    {{ finding.severity }}
                  </span>
                  <span class="text-xs font-medium text-slate-700">
                    {{ finding.scope }} / {{ finding.code }}
                  </span>
                </div>

                <p class="mt-3 text-sm text-slate-900">
                  {{ finding.message }}
                </p>

                @if (hasContext(finding)) {
                  <dl class="mt-3 grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 text-xs text-slate-600">
                    @if (finding.sheetName) {
                      <dt class="font-medium text-slate-500">Sheet</dt>
                      <dd class="text-slate-700">{{ finding.sheetName }}</dd>
                    }
                    @if (finding.rowNumber !== null) {
                      <dt class="font-medium text-slate-500">Row</dt>
                      <dd class="text-slate-700">{{ finding.rowNumber }}</dd>
                    }
                    @if (finding.columnName) {
                      <dt class="font-medium text-slate-500">Column</dt>
                      <dd class="text-slate-700">{{ finding.columnName }}</dd>
                    }
                    @if (finding.fieldName) {
                      <dt class="font-medium text-slate-500">Field</dt>
                      <dd class="text-slate-700">{{ finding.fieldName }}</dd>
                    }
                    @if (finding.rejectedValue !== null) {
                      <dt class="font-medium text-slate-500">Rejected</dt>
                      <dd class="text-slate-700">{{ finding.rejectedValue }}</dd>
                    }
                    @if (finding.expectedValue !== null) {
                      <dt class="font-medium text-slate-500">Expected</dt>
                      <dd class="text-slate-700">{{ finding.expectedValue }}</dd>
                    }
                    @if (finding.actualValue !== null) {
                      <dt class="font-medium text-slate-500">Actual</dt>
                      <dd class="text-slate-700">{{ finding.actualValue }}</dd>
                    }
                  </dl>
                }

                <div class="mt-3 flex flex-wrap items-center gap-2 text-xs text-slate-500">
                  <span>{{ finding.createdAt | date: 'medium' }}</span>
                </div>
              </li>

            }
          </ol>

        } @else {

          <app-page-state
            type="empty"
            title="No findings"
            message="No validation or processing issues were detected for this job."
          />

        }
      </div>
    </section>
  `
})
export class ProcessingJobFindingsList {

  readonly findings = input<ProcessingJobFindingResponse[]>([])
  readonly isLoading = input(false)
  readonly errorMessage = input<string | null>(null)

  protected getSeverityClass (severity: string): string {
    const level = severity.toUpperCase()
    if (level === 'ERROR') return 'bg-red-100 text-red-700'
    if (level === 'WARNING') return 'bg-amber-100 text-amber-700'
    if (level === 'INFO') return 'bg-blue-100 text-blue-700'
    return 'bg-slate-100 text-slate-700'
  }

  protected hasContext (finding: ProcessingJobFindingResponse): boolean {
    return !!(
      finding.sheetName ||
      finding.rowNumber !== null ||
      finding.columnName ||
      finding.fieldName ||
      finding.rejectedValue !== null ||
      finding.expectedValue !== null ||
      finding.actualValue !== null
    )
  }

}
