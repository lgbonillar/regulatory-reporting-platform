import { DatePipe } from '@angular/common'
import { HttpErrorResponse } from '@angular/common/http'
import { Component, computed, inject, OnInit, signal } from '@angular/core'

import { AppAlert } from '../../../../shared/components/app-alert/app-alert'
import { AppButton } from '../../../../shared/components/app-button/app-button'
import { AppPanel } from '../../../../shared/components/app-panel/app-panel'
import { FileDownloadLink } from '../../../../shared/components/file-download-link/file-download-link'
import { PageState } from '../../../../shared/components/page-state/page-state'
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge'
import { ReportFileUploadResponse, UploadedFileResponse } from '../../models/report-file-upload.model'
import { ReportFileUploadService } from '../../services/report-file-upload.service'

const CURRENT_USERNAME = 'analyst01'
const MINIMUM_FILES_SIZE = 0
const FIRST_FILE_INDEX = 0

@Component({
  selector: 'app-upload-report-page',
  imports: [ AppAlert, AppButton, AppPanel, DatePipe, FileDownloadLink, StatusBadge, PageState ],
  templateUrl: './upload-report-page.html'
})
export class UploadReportPage implements OnInit {
  private readonly reportFileUploadService = inject(ReportFileUploadService)

  protected readonly username = signal(CURRENT_USERNAME)
  protected readonly files = signal<UploadedFileResponse[]>([])
  protected readonly selectedFile = signal<File | null>(null)
  protected readonly uploadResult = signal<ReportFileUploadResponse | null>(null)
  protected readonly errorMessage = signal<string | null>(null)
  protected readonly successMessage = signal<string | null>(null)
  protected readonly isLoadingFiles = signal(false)
  protected readonly isUploading = signal(false)
  protected readonly actionFileId = signal<string | null>(null)

  protected readonly selectedFileSummary = computed(() => {
    const file = this.selectedFile()

    if (!file) return null

    return `${file.name} - ${file.size} bytes`
  })

  protected readonly hasFiles = computed(() => this.files().length > MINIMUM_FILES_SIZE)

  ngOnInit (): void {
    this.loadReportFiles()
  }

  protected onFileSelected (event: Event): void {
    const input = event.target as HTMLInputElement
    const file = input.files?.item(FIRST_FILE_INDEX) ?? null

    this.errorMessage.set(null)
    this.successMessage.set(null)
    this.uploadResult.set(null)
    this.selectedFile.set(file)
  }

  protected uploadSelectedFile (): void {
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
        this.errorMessage.set(this.resolveErrorMessage(error))
      },
      complete: () => {
        this.isUploading.set(false)
      }
    })
  }

  protected onReplacementFileSelected (event: Event, fileId: string): void {
    const input = event.target as HTMLInputElement
    const file = input.files?.item(FIRST_FILE_INDEX) ?? null

    if (!file) return

    this.actionFileId.set(fileId)
    this.errorMessage.set(null)
    this.successMessage.set(null)

    this.reportFileUploadService.updateReportFile(fileId, file).subscribe({
      next: () => {
        this.successMessage.set('File updated successfully.')
        this.loadReportFiles()
      },
      error: (error: unknown) => {
        this.errorMessage.set(this.resolveErrorMessage(error))
      },
      complete: () => {
        this.actionFileId.set(null)
        input.value = ''
      }
    })
  }

  protected deleteReportFile (fileId: string): void {
    this.actionFileId.set(fileId)
    this.errorMessage.set(null)
    this.successMessage.set(null)

    this.reportFileUploadService.deleteReportFile(fileId).subscribe({
      next: () => {
        this.successMessage.set('File deleted successfully.')
        this.loadReportFiles()
      },
      error: (error: unknown) => {
        this.errorMessage.set(this.resolveErrorMessage(error))
      },
      complete: () => {
        this.actionFileId.set(null)
      }
    })
  }

  protected isFileActionRunning (fileId: string): boolean {
    return this.actionFileId() === fileId
  }

  private loadReportFiles (): void {
    this.isLoadingFiles.set(true)

    this.reportFileUploadService.listReportFiles(this.username()).subscribe({
      next: (files) => {
        this.files.set(files)
      },
      error: (error: unknown) => {
        this.errorMessage.set(this.resolveErrorMessage(error))
      },
      complete: () => {
        this.isLoadingFiles.set(false)
      }
    })
  }

  private resolveErrorMessage (error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      return error.error?.message ?? 'Request failed. Please try again.'
    }

    return 'Unexpected error. Please try again.'
  }
}
