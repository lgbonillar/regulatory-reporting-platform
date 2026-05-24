import { Component, input, output } from '@angular/core'

import { AppButton } from '../../../../shared/components/app-button/app-button'

@Component({
  selector: 'app-report-file-upload-control',
  imports: [ AppButton ],
  template: `
    <div class="flex flex-col gap-4">
      <div>
        <p class="text-sm font-medium text-slate-700">Upload report file</p>
        <p class="mt-1 text-sm text-slate-500">
          Select an Excel file to create a processing flow.
        </p>
      </div>

      <div class="rounded-lg border border-dashed border-slate-300 bg-slate-50 p-4">
        <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
          <div class="min-w-0">
            <label
              class="inline-flex cursor-pointer items-center justify-center rounded-md border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 transition hover:border-slate-400 hover:bg-slate-50 focus-within:ring-2 focus-within:ring-slate-300"
              for="report-file"
            >
              Choose Excel file

              <input
                id="report-file"
                class="sr-only"
                type="file"
                accept=".xlsx"
                [disabled]="isUploading()"
                (change)="fileSelected.emit($event)"
              />
            </label>

            @if (selectedFileSummary()) {
              <p class="file-text mt-3 truncate text-sm text-slate-700">
                {{ selectedFileSummary() }}
              </p>
            } @else {
              <p class="mt-3 text-sm text-slate-500">
                No file selected.
              </p>
            }
          </div>

          <app-button
            variant="primary"
            [loading]="isUploading()"
            [disabled]="isUploading()"
            (click)="uploadRequested.emit()"
          >
            Upload
          </app-button>
        </div>
      </div>
    </div>
  `
})
export class ReportFileUploadControl {
  readonly selectedFileSummary = input<string | null>(null)
  readonly isUploading = input(false)

  readonly fileSelected = output<Event>()
  readonly uploadRequested = output<void>()
}
