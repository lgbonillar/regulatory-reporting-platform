import { Component, ElementRef, input, output, viewChild } from '@angular/core'
import { ButtonModule } from 'primeng/button'
import { FileUploadModule } from 'primeng/fileupload'
import { TooltipModule } from 'primeng/tooltip'

@Component({
  selector: 'app-report-file-upload-control',
  imports: [ ButtonModule, FileUploadModule, TooltipModule ],
  template: `
    @if (compact()) {
      <input
        #fileInput
        class="hidden"
        type="file"
        accept=".xlsx"
        [disabled]="isUploading()"
        (change)="fileInputChanged($event)"
      />

      <p-button
        styleClass="cursor-pointer"
        icon="fa-solid fa-upload"
        severity="secondary"
        [outlined]="true"
        [loading]="isUploading()"
        [disabled]="isUploading()"
        ariaLabel="Upload file"
        pTooltip="Upload file"
        tooltipPosition="top"
        showDelay="500"
        hideDelay="100"
        (click)="openFilePicker()"
      />
    } @else {
      <div class="flex flex-col gap-4">
        <div>
          <p class="text-sm font-medium text-slate-700">Upload report file</p>
          <p class="mt-1 text-sm text-slate-500">
            Select an Excel file to create a processing flow.
          </p>
        </div>

        <div class="rounded-lg border border-dashed border-slate-300 bg-slate-50 p-4">
          <div class="flex flex-col gap-4">
            <input
              #fileInput
              class="hidden"
              type="file"
              accept=".xlsx"
              [disabled]="isUploading()"
              (change)="fileInputChanged($event)"
            />

            <p-button
              styleClass="cursor-pointer"
              label="Choose Excel file"
              icon="fa-solid fa-file-excel"
              severity="secondary"
              [outlined]="true"
              [loading]="isUploading()"
              [disabled]="isUploading()"
              ariaLabel="Choose Excel file"
              (click)="openFilePicker()"
            />
          </div>
        </div>
      </div>
    }
  `
})
export class ReportFileUploadControl {
  private readonly fileInput = viewChild.required<ElementRef<HTMLInputElement>>('fileInput')

  readonly compact = input(false)
  readonly selectedFileSummary = input<string | null>(null)
  readonly isUploading = input(false)

  readonly fileSelected = output<File>()

  protected openFilePicker (): void {
    if (this.isUploading()) return

    this.fileInput().nativeElement.click()
  }

  protected fileInputChanged (event: Event): void {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]

    input.value = ''

    if (!file) return

    this.fileSelected.emit(file)
  }
}
