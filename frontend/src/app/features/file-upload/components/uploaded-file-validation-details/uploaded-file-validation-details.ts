import { DatePipe } from '@angular/common'
import { Component, input, output } from '@angular/core'

import { PageState } from '../../../../shared/components/page-state/page-state'
import { UploadedFileFindingResponse, UploadedFileValidationRunResponse } from '../../models/report-file-upload.model'

@Component({
  selector: 'app-uploaded-file-validation-details',
  imports: [ DatePipe, PageState ],
  template: `
    <section class="flex h-full min-h-0 flex-col">
      <div class="shrink-0 border-b border-slate-200 bg-white p-4 sm:p-6">
        <div class="flex items-start justify-between gap-4">
          <div>
            <h3 class="text-sm font-semibold text-slate-950">Validation details</h3>
            <p class="mt-1 text-sm text-slate-500">{{ filename() }}</p>
          </div>
          <button
            class="shrink-0 rounded-lg p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600 focus:outline-none focus-visible:ring-2 focus-visible:ring-slate-300"
            aria-label="Close"
            (click)="closeRequested.emit()"
          >
            <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>

      <div class="min-h-0 flex-1 overflow-auto p-4 sm:p-6">
        @if (isLoading()) {

          <app-page-state
            type="loading"
            title="Loading validation details"
            message="Please wait while the validation data is loaded."
          />

        } @else if (errorMessage()) {

          <app-page-state
            type="error"
            title="Validation details could not be loaded"
            [message]="errorMessage()"
          />

        } @else {

          @if (validationRuns().length > 0) {
            <div class="mb-6">
              <h4 class="text-xs font-semibold uppercase tracking-wide text-slate-500">Validation runs</h4>
              <ol class="mt-3 space-y-2">
                @for (run of validationRuns(); track run.validationRunId) {
                  <li class="rounded-lg border border-slate-200 bg-slate-50 p-3">
                    <div class="flex flex-wrap items-center gap-2">
                      <span
                        class="inline-flex items-center rounded px-2 py-0.5 text-xs font-medium"
                        [class]="runStatusClass(run.status)"
                      >
                        {{ run.status }}
                      </span>
                      <span class="text-sm text-slate-700">{{ run.summaryMessage }}</span>
                    </div>
                    <div class="mt-2 flex flex-wrap items-center gap-2 text-xs text-slate-500">
                      <span>{{ run.source }}</span>
                      <span aria-hidden="true">·</span>
                      <span>by {{ run.createdBy }}</span>
                      <span aria-hidden="true">·</span>
                      <span>{{ run.createdAt | date: 'medium' }}</span>
                    </div>
                  </li>
                }
              </ol>
            </div>
          }

          @if (findings().length > 0) {
            <div>
              <h4 class="text-xs font-semibold uppercase tracking-wide text-slate-500">Findings</h4>
              <ol class="mt-3 space-y-3">
                @for (finding of findings(); track finding.findingId) {
                  <li class="rounded-lg border border-slate-200 bg-slate-50 p-4">
                    <div class="flex flex-wrap items-center gap-2">
                      <span
                        class="inline-flex items-center rounded px-2 py-0.5 text-xs font-medium"
                        [class]="severityClass(finding.severity)"
                      >
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
            </div>
          } @else {
            <app-page-state
              type="empty"
              title="No findings"
              message="No validation issues were detected for this file."
            />
          }

        }
      </div>
    </section>
  `
})
export class UploadedFileValidationDetails {
  readonly filename = input<string>('')
  readonly validationRuns = input<UploadedFileValidationRunResponse[]>([])
  readonly findings = input<UploadedFileFindingResponse[]>([])
  readonly isLoading = input(false)
  readonly errorMessage = input<string | null>(null)
  readonly closeRequested = output<void>()

  protected severityClass (severity: string): string {
    const level = severity.toUpperCase()
    if (level === 'ERROR') return 'bg-red-100 text-red-700'
    if (level === 'WARNING') return 'bg-amber-100 text-amber-700'
    if (level === 'INFO') return 'bg-blue-100 text-blue-700'
    return 'bg-slate-100 text-slate-700'
  }

  protected runStatusClass (status: string): string {
    const level = status.toUpperCase()
    if (level === 'PASSED') return 'bg-green-100 text-green-700'
    if (level === 'FAILED') return 'bg-red-100 text-red-700'
    return 'bg-slate-100 text-slate-700'
  }

  protected hasContext (finding: UploadedFileFindingResponse): boolean {
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
