import { HttpErrorResponse } from '@angular/common/http'
import { Component, computed, inject, signal } from '@angular/core'

import { ReportFileUploadResponse } from '../../models/report-file-upload.model'
import { ReportFileUploadService } from '../../services/report-file-upload.service'

@Component({
  selector: 'app-upload-report-page',
  templateUrl: './upload-report-page.html'
})
export class UploadReportPage {
  private readonly reportFileUploadService = inject(ReportFileUploadService)

  protected readonly errorMessage = signal<string | null>(null)
  protected readonly isUploading = signal(false)
  protected readonly selectedFile = signal<File | null>(null)
  protected readonly uploadResult = signal<ReportFileUploadResponse | null>(null)
  protected readonly selectedFileSummary = computed(() => {
    const file = this.selectedFile()

    if (!file) {
      return null
    }

    return `${file.name} - ${file.size} bytes`
  })
  protected readonly downloadUrl = computed(() => {
    const result = this.uploadResult()

    if (!result) {
      return null
    }

    return this.reportFileUploadService.getDownloadUrl(result.fileId)
  })

  protected onFileSelected (event: Event): void {
    const input = event.target as HTMLInputElement
    const file = input.files?.item(Number(false)) ?? null

    this.errorMessage.set(null)
    this.uploadResult.set(null)
    this.selectedFile.set(file)
  }

  protected uploadSelectedFile (): void {
    const file = this.selectedFile()

    if (!file) {
      this.errorMessage.set('Select an .xlsx file before uploading.')
      return
    }

    this.isUploading.set(true)
    this.errorMessage.set(null)

    this.reportFileUploadService.uploadReportFile(file).subscribe({
      next: (response): void => {
        this.uploadResult.set(response)
      },
      error: (error: unknown): void => {
        this.errorMessage.set(this.resolveErrorMessage(error))
        this.isUploading.set(false)
      },
      complete: (): void => {
        this.isUploading.set(false)
      }
    })
  }

  private resolveErrorMessage (error: unknown): string {
    if (error instanceof HttpErrorResponse && typeof error.error?.message === 'string') {
      return error.error.message
    }

    return 'The report file could not be uploaded.'
  }
}
