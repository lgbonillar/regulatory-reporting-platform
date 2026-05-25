import { Component, inject, OnInit, signal } from '@angular/core'

import { AppAlert } from '../../../../shared/components/app-alert/app-alert'
import { AppPanel } from '../../../../shared/components/app-panel/app-panel'
import { ConfirmationDialog } from '../../../../shared/components/confirmation-dialog/confirmation-dialog'
import { PageHeader } from '../../../../shared/components/page-header/page-header'
import { PageState } from '../../../../shared/components/page-state/page-state'
import { ReportFileUploadControl } from '../../components/report-file-upload-control/report-file-upload-control'
import { UploadedFilesList } from '../../components/uploaded-files-list/uploaded-files-list'
import { UploadReportPageStore } from './upload-report-page.store'

const FIRST_FILE_INDEX = 0

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

  protected onFileSelected (event: Event): void {
    const input = event.target as HTMLInputElement
    const file = input.files?.item(FIRST_FILE_INDEX) ?? null

    this.store.setSelectedFile(file)
  }

  protected onReplacementFileSelected (event: Event, fileId: string): void {
    const input = event.target as HTMLInputElement
    const file = input.files?.item(FIRST_FILE_INDEX) ?? null

    if (!file) return

    this.store.updateReportFile(fileId, file)
    input.value = ''
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
}
