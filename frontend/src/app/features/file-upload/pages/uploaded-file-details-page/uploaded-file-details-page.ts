import { DatePipe } from '@angular/common'
import { Component, inject, OnInit } from '@angular/core'
import { RouterLink } from '@angular/router'

import { AppAlert } from '../../../../shared/components/app-alert/app-alert'
import { AppBreadcrumb, AppBreadcrumbItem } from '../../../../shared/components/app-breadcrumb/app-breadcrumb'
import { PageHeader } from '../../../../shared/components/page-header/page-header'
import { PageState } from '../../../../shared/components/page-state/page-state'
import { UploadedFileFindingResponse, UploadedFileValidationRunResponse } from '../../models/report-file-upload.model'
import { UploadedFileDetailsPageStore } from './uploaded-file-details-page.store'

@Component({
  selector: 'app-uploaded-file-details-page',
  host: {
    class: 'block h-full min-h-0'
  },
  imports: [
    AppAlert,
    AppBreadcrumb,
    DatePipe,
    PageHeader,
    PageState,
    RouterLink
  ],
  providers: [ UploadedFileDetailsPageStore ],
  template: `
    <section class="flex h-full min-h-0 w-full flex-col gap-4">
      <app-breadcrumb [items]="breadcrumbItems" />

      <app-page-header title="File details">
        <a
          routerLink="/report-files/upload"
          class="inline-flex items-center text-sm font-medium text-slate-600 underline decoration-slate-300 underline-offset-4 transition hover:text-slate-950 hover:decoration-slate-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-slate-300"
        >
          ← Back to files
        </a>
      </app-page-header>

      @if (store.errorMessage()) {
        <app-alert type="error" [message]="store.errorMessage()!" />
      }

      @if (store.isLoading()) {
        <app-page-state
          type="loading"
          title="Loading file"
          message="Please wait while the file is loaded."
        />
      } @else if (store.file()) {
        <div class="min-h-0 flex-1 overflow-hidden">
          <div class="grid h-full min-h-0 grid-cols-2 gap-6">
            <div class="flex min-h-0 flex-col overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
              <div class="shrink-0 border-b border-slate-200 bg-white p-4 sm:p-6">
                <h3 class="text-sm font-semibold text-slate-950">Validation runs</h3>
                <p class="mt-1 text-sm text-slate-500">Validation history for this file.</p>
              </div>
              <div class="min-h-0 flex-1 overflow-auto p-4 sm:p-6">
                @if (store.isValidationDetailsLoading()) {
                  <app-page-state
                    type="loading"
                    title="Loading validation runs"
                    message="Please wait while the data is loaded."
                  />
                } @else if (store.validationDetailsErrorMessage()) {
                  <app-page-state
                    type="error"
                    title="Validation runs could not be loaded"
                    [message]="store.validationDetailsErrorMessage()!"
                  />
                } @else if (store.validationRuns().length > 0) {
                  <ol class="space-y-3">
                    @for (run of store.validationRuns(); track run.validationRunId) {
                      <li class="rounded-lg border border-slate-200 bg-slate-50 p-4">
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
                } @else {
                  <app-page-state
                    type="empty"
                    title="No validation runs"
                    message="No validation runs have been recorded for this file."
                  />
                }
              </div>
            </div>

            <div class="flex min-h-0 flex-col overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
              <div class="shrink-0 border-b border-slate-200 bg-white p-4 sm:p-6">
                <h3 class="text-sm font-semibold text-slate-950">Findings</h3>
                <p class="mt-1 text-sm text-slate-500">Validation issues detected in this file.</p>
              </div>
              <div class="min-h-0 flex-1 overflow-auto p-4 sm:p-6">
                @if (store.isValidationDetailsLoading()) {
                  <app-page-state
                    type="loading"
                    title="Loading findings"
                    message="Please wait while the findings are loaded."
                  />
                } @else if (store.validationDetailsErrorMessage()) {
                  <app-page-state
                    type="error"
                    title="Findings could not be loaded"
                    [message]="store.validationDetailsErrorMessage()!"
                  />
                } @else if (store.fileFindings().length > 0) {
                  <ol class="space-y-3">
                    @for (finding of store.fileFindings(); track finding.findingId) {
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
                        <p class="mt-3 text-sm text-slate-900">{{ finding.message }}</p>
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
                    message="No validation issues were detected for this file."
                  />
                }
              </div>
            </div>
          </div>
        </div>
      }
    </section>
  `
})
export class UploadedFileDetailsPage implements OnInit {
  protected readonly store = inject(UploadedFileDetailsPageStore)

  ngOnInit (): void {
    this.store.loadFile()
  }

  protected get breadcrumbItems () {
    return [
      { label: 'report-files', route: '/report-files/upload' },
      { label: this.store.file()?.originalFilename ?? 'details' }
    ] as AppBreadcrumbItem[]
  }

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
