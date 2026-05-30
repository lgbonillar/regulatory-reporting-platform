import { computed, inject, Injectable, signal } from '@angular/core'
import { ActivatedRoute, Router } from '@angular/router'
import { forkJoin } from 'rxjs'
import { finalize } from 'rxjs/operators'

import { SessionService } from '../../../../core/auth/session.service'
import { AppToastService } from '../../../../shared/services/app-toast.service'
import { resolveHttpErrorMessage } from '../../../../shared/utils/http-error-message'
import { UploadedFileFindingResponse, UploadedFileResponse, UploadedFileValidationRunResponse } from '../../models/report-file-upload.model'
import { ReportFileUploadService } from '../../services/report-file-upload.service'

@Injectable()
export class UploadedFileDetailsPageStore {
  private readonly route = inject(ActivatedRoute)
  private readonly router = inject(Router)
  private readonly reportFileUploadService = inject(ReportFileUploadService)
  private readonly sessionService = inject(SessionService)
  private readonly toast = inject(AppToastService)

  readonly file = signal<UploadedFileResponse | null>(null)
  readonly validationRuns = signal<UploadedFileValidationRunResponse[]>([])
  readonly fileFindings = signal<UploadedFileFindingResponse[]>([])
  readonly errorMessage = signal<string | null>(null)
  readonly isLoading = signal(false)
  readonly isValidationDetailsLoading = signal(false)
  readonly validationDetailsErrorMessage = signal<string | null>(null)

  readonly fileId = computed(() => this.route.snapshot.paramMap.get('fileId'))
  readonly username = computed(() => this.sessionService.currentUser()?.username ?? null)

  loadFile (): void {
    const fileId = this.fileId()
    const username = this.username()

    if (!fileId) {
      this.errorMessage.set('File ID is missing.')
      return
    }

    if (!username) {
      this.errorMessage.set('User session not found. Please log in again.')
      return
    }

    this.isLoading.set(true)
    this.errorMessage.set(null)

    this.reportFileUploadService.listReportFiles(username).subscribe({
      next: (files) => {
        const found = files.find((f) => f.fileId === fileId)

        if (!found) {
          this.file.set(null)
          this.errorMessage.set('File not found.')
          return
        }

        this.file.set(found)
        this.loadValidationDetails(fileId)
      },
      error: (error: unknown) => {
        this.errorMessage.set(resolveHttpErrorMessage(error))
      },
      complete: () => {
        this.isLoading.set(false)
      }
    })
  }

  private loadValidationDetails (fileId: string): void {
    this.isValidationDetailsLoading.set(true)
    this.validationDetailsErrorMessage.set(null)

    forkJoin({
      runs: this.reportFileUploadService.listValidationRuns(fileId),
      findings: this.reportFileUploadService.listFindings(fileId)
    }).pipe(
      finalize(() => this.isValidationDetailsLoading.set(false))
    ).subscribe({
      next: ({ runs, findings }) => {
        this.validationRuns.set(runs)
        this.fileFindings.set(findings)
      },
      error: (error: unknown) => {
        this.validationRuns.set([])
        this.fileFindings.set([])
        this.validationDetailsErrorMessage.set(resolveHttpErrorMessage(error))
      }
    })
  }
}
