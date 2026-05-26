import { DatePipe } from '@angular/common'
import { Component, input, output } from '@angular/core'
import { TableModule } from 'primeng/table'

import { AppButton } from '../../../../shared/components/app-button/app-button'
import { CopyableCode } from '../../../../shared/components/copyable-code/copyable-code'
import { FileDownloadLink } from '../../../../shared/components/file-download-link/file-download-link'
import { FilePickerButton } from '../../../../shared/components/file-picker-button/file-picker-button'
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge'
import { UploadedFileResponse } from '../../models/report-file-upload.model'

@Component({
  selector: 'app-uploaded-files-list',
  host: {
    class: 'block h-full min-h-0'
  },
  imports: [ AppButton, CopyableCode, DatePipe, FileDownloadLink, FilePickerButton, StatusBadge, TableModule ],
  template: `
    <div class="hidden h-full min-h-0 md:block">
      <p-table
        class="h-full! text-sm!"
        [value]="files()"
        [scrollable]="true"
        scrollHeight="flex"
        dataKey="fileId"
      >
        <ng-template #header>
          <tr>
            <th>File</th>
            <th>Status</th>
            <th>Uploaded</th>
            <th class="text-right">Actions</th>
          </tr>
        </ng-template>

        <ng-template #body let-file>
          <tr>
            <td>
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

            <td>
              <app-status-badge [status]="file.fileStatus" />
            </td>

            <td class="text-slate-600">
              {{ file.uploadedAt | date: 'medium' }}
            </td>

            <td>
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
        </ng-template>
      </p-table>
    </div>

    <div class="h-full min-h-0 divide-y divide-slate-200 overflow-auto md:hidden">
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
  readonly replacementSelected = output<{ event: { files?: File[] }, fileId: string }>()
  readonly deleteSelected = output<string>()

  protected isActionRunning (fileId: string): boolean {
    return this.actionFileId() === fileId
  }
}
