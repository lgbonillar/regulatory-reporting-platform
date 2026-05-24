import { Component, inject, OnInit } from '@angular/core'

import { AppAlert } from '../../../../shared/components/app-alert/app-alert'
import { AppButton } from '../../../../shared/components/app-button/app-button'
import { AppPanel } from '../../../../shared/components/app-panel/app-panel'
import { PageHeader } from '../../../../shared/components/page-header/page-header'
import { PageState } from '../../../../shared/components/page-state/page-state'
import { UploadedFilesList } from '../../components/uploaded-files-list/uploaded-files-list'
import { UploadReportPageStore } from './upload-report-page.store'

const FIRST_FILE_INDEX = 0

@Component({
  selector: 'app-upload-report-page',
  imports: [ AppAlert, AppButton, AppPanel, PageHeader, PageState, UploadedFilesList ],
  providers: [ UploadReportPageStore ],
  templateUrl: './upload-report-page.html'
})
export class UploadReportPage implements OnInit {
  protected readonly store = inject(UploadReportPageStore)

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
}
