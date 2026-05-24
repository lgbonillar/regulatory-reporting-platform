import { computed, inject, Injectable, signal } from '@angular/core'

import { resolveHttpErrorMessage } from '../../../../shared/utils/http-error-message'
import { ReportFileUploadResponse, UploadedFileResponse } from '../../models/report-file-upload.model'
import { ReportFileUploadService } from '../../services/report-file-upload.service'

const CURRENT_USERNAME = 'analyst01'
const MINIMUM_FILES_SIZE = 0

@Injectable()
export class UploadReportPageStore {
  private readonly reportFileUploadService = inject(ReportFileUploadService)

  readonly username = signal(CURRENT_USERNAME)
  readonly files = signal<UploadedFileResponse[]>([])
  readonly selectedFile = signal<File | null>(null)
  readonly uploadResult = signal<ReportFileUploadResponse | null>(null)
  readonly errorMessage = signal<string | null>(null)
  readonly successMessage = signal<string | null>(null)
  readonly isLoadingFiles = signal(false)
  readonly isUploading = signal(false)
  readonly actionFileId = signal<string | null>(null)

  readonly selectedFileSummary = computed(() => {
    const file = this.selectedFile()

    if (!file) return null

    return `${file.name} - ${file.size} bytes`
  })

  readonly hasFiles = computed(() => this.files().length > MINIMUM_FILES_SIZE)

  setSelectedFile (file: File | null): void {
    this.errorMessage.set(null)
    this.successMessage.set(null)
    this.uploadResult.set(null)
    this.selectedFile.set(file)
  }

  uploadSelectedFile (): void {
    const file = this.selectedFile()

    if (!file) {
      this.errorMessage.set('Select an Excel file before uploading.')
      return
    }

    this.isUploading.set(true)
    this.errorMessage.set(null)
    this.successMessage.set(null)

    this.reportFileUploadService.uploadReportFile(file).subscribe({
      next: (response) => {
        this.uploadResult.set(response)
        this.selectedFile.set(null)
        this.successMessage.set('File uploaded successfully.')
        this.loadReportFiles()
      },
      error: (error: unknown) => {
        this.errorMessage.set(resolveHttpErrorMessage(error))
      },
      complete: () => {
        this.isUploading.set(false)
      }
    })
  }

  updateReportFile (fileId: string, file: File): void {
    this.actionFileId.set(fileId)
    this.errorMessage.set(null)
    this.successMessage.set(null)

    this.reportFileUploadService.updateReportFile(fileId, file).subscribe({
      next: () => {
        this.successMessage.set('File updated successfully.')
        this.loadReportFiles()
      },
      error: (error: unknown) => {
        this.errorMessage.set(resolveHttpErrorMessage(error))
      },
      complete: () => {
        this.actionFileId.set(null)
      }
    })
  }

  deleteReportFile (fileId: string): void {
    this.actionFileId.set(fileId)
    this.errorMessage.set(null)
    this.successMessage.set(null)

    this.reportFileUploadService.deleteReportFile(fileId).subscribe({
      next: () => {
        this.successMessage.set('File deleted successfully.')
        this.loadReportFiles()
      },
      error: (error: unknown) => {
        this.errorMessage.set(resolveHttpErrorMessage(error))
      },
      complete: () => {
        this.actionFileId.set(null)
      }
    })
  }

  loadReportFiles (): void {
    this.isLoadingFiles.set(true)

    this.reportFileUploadService.listReportFiles(this.username()).subscribe({
      next: (files) => {
        this.files.set(files)
      },
      error: (error: unknown) => {
        this.errorMessage.set(resolveHttpErrorMessage(error))
      },
      complete: () => {
        this.isLoadingFiles.set(false)
      }
    })
  }

}
