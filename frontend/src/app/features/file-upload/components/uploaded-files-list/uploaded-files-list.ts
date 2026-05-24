import { DatePipe } from '@angular/common'
import { Component, input, output } from '@angular/core'

import { AppButton } from '../../../../shared/components/app-button/app-button'
import { CopyableCode } from '../../../../shared/components/copyable-code/copyable-code'
import { FileDownloadLink } from '../../../../shared/components/file-download-link/file-download-link'
import { FilePickerButton } from '../../../../shared/components/file-picker-button/file-picker-button'
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge'
import { UploadedFileResponse } from '../../models/report-file-upload.model'

@Component({
  selector: 'app-uploaded-files-list',
  imports: [ AppButton, CopyableCode, DatePipe, FileDownloadLink, FilePickerButton, StatusBadge ],
  template: `
    <div class="hidden overflow-x-auto md:block">
      <table class="min-w-full divide-y divide-slate-200 text-left text-sm">
        <thead class="bg-slate-50 text-xs font-semibold uppercase text-slate-500">
          <tr>
            <th class="px-6 py-3">File</th>
            <th class="px-6 py-3">Status</th>
            <th class="px-6 py-3">Uploaded</th>
            <th class="px-6 py-3 text-right">Actions</th>
          </tr>
        </thead>

        <tbody class="divide-y divide-slate-200">
          @for (file of files(); track file.fileId) {
            <tr class="align-middle">
              <td class="px-6 py-4">
                <div class="max-w-md">
                  <app-file-download-link
                    [fileId]="file.fileId"
                    [filename]="file.originalFilename"
                    [fileStatus]="file.fileStatus"
                  />

                  <div class="mt-1">
                    <app-copyable-code [value]="file.fileId" [ariaLabel]="'Copy file ID'" />
                  </div>
                </div>
              </td>

              <td class="px-6 py-4">
                <app-status-badge [status]="file.fileStatus" />
              </td>

              <td class="px-6 py-4 text-slate-600">
                {{ file.uploadedAt | date: 'medium' }}
              </td>

              <td class="px-6 py-4">
                <div class="flex items-center justify-end gap-2">
                  <app-file-picker-button
                    label="Update"
                    accept=".xlsx"
                    [disabled]="isActionRunning(file.fileId)"
                    (fileSelected)="replacementSelected.emit({ event: $event, fileId: file.fileId })"
                  />

                  <app-button
                    variant="danger"
                    [disabled]="isActionRunning(file.fileId)"
                    (click)="deleteSelected.emit(file.fileId)"
                  >
                    Delete
                  </app-button>
                </div>
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>

    <div class="divide-y divide-slate-200 md:hidden">
      @for (file of files(); track file.fileId) {
        <article class="flex flex-col gap-4 px-4 py-4">
          <div>
            <app-file-download-link
              [fileId]="file.fileId"
              [filename]="file.originalFilename"
              [fileStatus]="file.fileStatus"
            />

            <div class="mt-1">
              <app-copyable-code [value]="file.fileId" [ariaLabel]="'Copy file ID'" />
            </div>
          </div>

          <div class="flex flex-wrap items-center gap-2 text-sm">
            <app-status-badge [status]="file.fileStatus" />

            <span class="text-slate-500">
              {{ file.uploadedAt | date: 'mediumDate' }}
            </span>
          </div>

          <div class="grid grid-cols-1 gap-2 sm:grid-cols-2">
            <app-file-picker-button
              label="Update"
              accept=".xlsx"
              [disabled]="isActionRunning(file.fileId)"
              (fileSelected)="replacementSelected.emit({ event: $event, fileId: file.fileId })"
            />

            <app-button
              variant="danger"
              [disabled]="isActionRunning(file.fileId)"
              (click)="deleteSelected.emit(file.fileId)"
            >
              Delete
            </app-button>
          </div>
        </article>
      }
    </div>
  `
})
export class UploadedFilesList {
  readonly files = input.required<UploadedFileResponse[]>()
  readonly actionFileId = input<string | null>(null)
  readonly replacementSelected = output<{ event: Event, fileId: string }>()
  readonly deleteSelected = output<string>()

  protected isActionRunning (fileId: string): boolean {
    return this.actionFileId() === fileId
  }
}
