import { Component, input, output } from '@angular/core'
import { FileUploadModule } from 'primeng/fileupload'

import { AppButton } from '../../../../shared/components/app-button/app-button'

@Component({
  selector: 'app-report-file-upload-control',
  imports: [ AppButton, FileUploadModule ],
  template: `
    <div class="flex flex-col gap-4">
      <div>
        <p class="text-sm font-medium text-slate-700">Upload report file</p>
        <p class="mt-1 text-sm text-slate-500">
          Select an Excel file to create a processing flow.
        </p>
      </div>

      <div class="rounded-lg border border-dashed border-slate-300 bg-slate-50 p-4">
        <div class="flex flex-col gap-4">
          <p-fileupload
            mode="basic"
            name="file"
            accept=".xlsx"
            chooseLabel="Choose Excel file"
            chooseIcon="fa-solid fa-file-excel"
            [auto]="false"
            [customUpload]="true"
            [disabled]="isUploading()"
            (onSelect)="fileUploadSelected($event)"
          />

          @if (selectedFileSummary()) {
            <p class="file-text truncate text-sm text-slate-700">
              {{ selectedFileSummary() }}
            </p>
          } @else {
            <p class="text-sm text-slate-500">
              No file selected.
            </p>
          }

          <div class="flex justify-end">
            <app-button
              variant="primary"
              [loading]="isUploading()"
              [disabled]="isUploading() || !selectedFileSummary()"
              (click)="uploadRequested.emit()"
            >
              Upload
            </app-button>
          </div>
        </div>
      </div>
    </div>
  `
})
export class ReportFileUploadControl {
  readonly selectedFileSummary = input<string | null>(null)
  readonly isUploading = input(false)

  readonly fileSelected = output<Event | { files?: File[] }>()
  readonly uploadRequested = output<void>()

  protected fileUploadSelected (event: { files?: File[] }): void {
    this.fileSelected.emit(event)
  }
}
