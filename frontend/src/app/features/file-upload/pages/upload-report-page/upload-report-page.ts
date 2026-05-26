import { Component, inject, OnInit, signal } from '@angular/core'

import { AppAlert } from '../../../../shared/components/app-alert/app-alert'
import { AppPanel } from '../../../../shared/components/app-panel/app-panel'
import { ConfirmationDialog } from '../../../../shared/components/confirmation-dialog/confirmation-dialog'
import { PageHeader } from '../../../../shared/components/page-header/page-header'
import { PageState } from '../../../../shared/components/page-state/page-state'
import { ReportFileUploadControl } from '../../components/report-file-upload-control/report-file-upload-control'
import { UploadedFilesList } from '../../components/uploaded-files-list/uploaded-files-list'
import { UploadReportPageStore } from './upload-report-page.store'

@Component({
  selector: 'app-upload-report-page',
  host: {
    class: 'block h-full min-h-0'
  },
  imports: [ AppAlert, AppPanel, ConfirmationDialog, PageHeader, PageState, ReportFileUploadControl, UploadedFilesList ],
  providers: [ UploadReportPageStore ],
  templateUrl: './upload-report-page.html'
})
export class UploadReportPage implements OnInit {
  protected readonly store = inject(UploadReportPageStore)
  protected readonly pendingDeleteFileId = signal<string | null>(null)

  ngOnInit (): void {
    this.store.loadReportFiles()
  }

  protected onFileSelected (event: Event | { files?: File[] }): void {
    const file = this.getSelectedFile(event)

    this.store.setSelectedFile(file)
  }

  protected onReplacementFileSelected (event: Event | { files?: File[] }, fileId: string): void {
    const file = this.getSelectedFile(event)

    if (!file) return

    this.store.updateReportFile(fileId, file)
  }

  protected requestDeleteReportFile (fileId: string): void {
    this.pendingDeleteFileId.set(fileId)
  }

  protected cancelDeleteReportFile (): void {
    this.pendingDeleteFileId.set(null)
  }

  protected confirmDeleteReportFile (): void {
    const fileId = this.pendingDeleteFileId()

    if (!fileId) return

    this.store.deleteReportFile(fileId)
    this.pendingDeleteFileId.set(null)
  }

  private getSelectedFile (event: Event | { files?: File[] }): File | null {
    if (this.isPrimeFileUploadEvent(event)) {
      return event.files?.[0] ?? null
    }

    const input = event.target as HTMLInputElement | null

    return input?.files?.[0] ?? null
  }

  private isPrimeFileUploadEvent (event: Event | { files?: File[] }): event is { files?: File[] } {
    return 'files' in event
  }
}
