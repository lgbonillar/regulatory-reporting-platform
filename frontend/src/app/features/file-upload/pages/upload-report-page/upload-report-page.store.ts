import { computed, inject, Injectable, signal } from '@angular/core'

import { SessionService } from '../../../../core/auth/session.service'
import { AppToastService } from '../../../../shared/services/app-toast.service'
import { resolveHttpErrorMessage } from '../../../../shared/utils/http-error-message'
import { ReportFileUploadResponse, UploadedFileResponse } from '../../models/report-file-upload.model'
import { ReportFileUploadService } from '../../services/report-file-upload.service'

const MINIMUM_FILES_SIZE = 0

@Injectable()
export class UploadReportPageStore {
  private readonly reportFileUploadService = inject(ReportFileUploadService)
  private readonly sessionService = inject(SessionService)
  private readonly toast = inject(AppToastService)

  readonly username = computed(() => this.sessionService.currentUser()?.username ?? null)
  readonly files = signal<UploadedFileResponse[]>([])
  readonly selectedFile = signal<File | null>(null)
  readonly uploadResult = signal<ReportFileUploadResponse | null>(null)
  readonly errorMessage = signal<string | null>(null)
  readonly isLoadingFiles = signal(false)
  readonly isUploading = signal(false)
  readonly actionFileId = signal<string | null>(null)

  readonly selectedFileSummary = computed(() => {
    const file = this.selectedFile()

    if (!file) return null

    return `${file.name} - ${file.size} bytes`
  })

  readonly hasFiles = computed(() => this.files().length > MINIMUM_FILES_SIZE)

  updateReportFile (fileId: string, file: File): void {
    this.actionFileId.set(fileId)
    this.errorMessage.set(null)

    this.reportFileUploadService.updateReportFile(fileId, file).subscribe({
      next: () => {
        this.toast.success(`File ${file.name} updated`)
        this.loadReportFiles()
      },
      error: (error: unknown) => {
        const message = resolveHttpErrorMessage(error)

        this.errorMessage.set(message)
        this.toast.error('Could not update file', message)
      },
      complete: () => {
        this.actionFileId.set(null)
      }
    })
  }

  deleteReportFile (fileId: string): void {
    this.actionFileId.set(fileId)
    this.errorMessage.set(null)

    this.reportFileUploadService.deleteReportFile(fileId).subscribe({
      next: () => {
        this.toast.success('File deleted successfully')
        this.loadReportFiles()
      },
      error: (error: unknown) => {
        const message = resolveHttpErrorMessage(error)

        this.errorMessage.set(message)
        this.toast.error('Could not delete file', message)
      },
      complete: () => {
        this.actionFileId.set(null)
      }
    })
  }

  loadReportFiles (): void {
    const username = this.username()

    if (!username) {
      this.errorMessage.set('User session not found. Please log in again.')
      this.isLoadingFiles.set(false)
      return
    }

    this.isLoadingFiles.set(true)

    this.reportFileUploadService.listReportFiles(username).subscribe({
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

  uploadFile (file: File): void {
    this.selectedFile.set(file)
    this.isUploading.set(true)
    this.errorMessage.set(null)

    this.reportFileUploadService.uploadReportFile(file).subscribe({
      next: (response) => {
        this.uploadResult.set(response)
        this.selectedFile.set(null)
        this.toast.success(`File ${file.name} uploaded`)
        this.loadReportFiles()
      },
      error: (error: unknown) => {
        const message = resolveHttpErrorMessage(error)

        this.errorMessage.set(message)
        this.toast.error('Could not upload file', message)
      },
      complete: () => {
        this.isUploading.set(false)
      }
    })
  }

}
