import { DatePipe } from '@angular/common'
import { Component, computed, input, output } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { ButtonModule } from 'primeng/button'
import { DatePickerModule } from 'primeng/datepicker'
import { MultiSelectModule } from 'primeng/multiselect'
import { TableModule } from 'primeng/table'
import { TooltipModule } from 'primeng/tooltip'

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
  imports: [
    ButtonModule,
    CopyableCode,
    DatePickerModule,
    DatePipe,
    FileDownloadLink,
    FilePickerButton,
    FormsModule,
    MultiSelectModule,
    StatusBadge,
    TableModule,
    TooltipModule
  ],
  template: `
    <div class="hidden h-full min-h-0 md:block">
      <p-table
        class="h-full! text-sm!"
        [value]="tableFiles()"
        [scrollable]="true"
        scrollHeight="flex"
        dataKey="fileId"
      >
        <ng-template #header>
          <tr>
            <th>
              <div class="flex items-center justify-between gap-2">
                <span>File</span>
                <p-columnFilter
                  field="originalFilenameFilter"
                  display="menu"
                  matchMode="contains"
                  [showMatchModes]="false"
                  [showOperator]="false"
                  [showAddButton]="false"
                  [showApplyButton]="false"
                >
                  <ng-template #filter let-value let-filter="filterCallback">
                    <input
                      class="w-64 rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 outline-none transition focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
                      type="text"
                      placeholder="Filter by file name"
                      [ngModel]="value"
                      (ngModelChange)="filter($event ? $event.toLowerCase() : null)"
                    />
                  </ng-template>
                </p-columnFilter>
              </div>
            </th>
            <th>
              <div class="flex items-center justify-between gap-2">
                <span>Status</span>
                <p-columnFilter
                  field="fileStatus"
                  matchMode="in"
                  display="menu"
                  [showMatchModes]="false"
                  [showOperator]="false"
                  [showAddButton]="false"
                  [showApplyButton]="false"
                >
                  <ng-template #filter let-value let-filter="filterCallback">
                    <p-multiselect
                      class="w-64"
                      optionLabel="label"
                      optionValue="value"
                      placeholder="Select statuses"
                      [options]="fileStatusOptions()"
                      [ngModel]="value"
                      (ngModelChange)="filter($event?.length ? $event : null)"
                    >
                      <ng-template #item let-option>
                        <app-status-badge [status]="option.value" />
                      </ng-template>

                      <ng-template #selectedItems let-value>
                        @if (value?.length) {
                          <span class="text-sm text-slate-700">
                            {{ value.length }} selected
                          </span>
                        } @else {
                          <span class="text-sm text-slate-400">Select statuses</span>
                        }
                      </ng-template>
                    </p-multiselect>
                  </ng-template>
                </p-columnFilter>
              </div>
            </th>
            <th>
              <div class="flex items-center justify-between gap-2">
                <span>Uploaded</span>
                <p-columnFilter
                  field="uploadedAtDate"
                  type="date"
                  display="menu"
                  matchMode="between"
                  [showMatchModes]="false"
                  [showOperator]="false"
                  [showAddButton]="false"
                  [showApplyButton]="false"
                >
                  <ng-template #filter let-value let-filter="filterCallback">
                    <p-datepicker
                      class="w-72"
                      selectionMode="range"
                      placeholder="Select date range"
                      [ngModel]="value"
                      (ngModelChange)="filter($event)"
                      [showIcon]="true"
                    />
                  </ng-template>
                </p-columnFilter>
              </div>
            </th>
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
                  accept=".xlsx"
                  [disabled]="isActionRunning(file.fileId)"
                  (fileSelected)="replacementSelected.emit({ file: $event, fileId: file.fileId })"
                />

                <p-button
                  styleClass="cursor-pointer"
                  icon="fa-regular fa-trash-can"
                  severity="danger"
                  [outlined]="true"
                  [disabled]="isActionRunning(file.fileId)"
                  pTooltip="Delete file"
                  tooltipPosition="top"
                  showDelay="500"
                  hideDelay="100"
                  ariaLabel="Delete file"
                  (click)="deleteSelected.emit(file.fileId)"
                />
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
              accept=".xlsx"
              [disabled]="isActionRunning(file.fileId)"
              (fileSelected)="replacementSelected.emit({ file: $event, fileId: file.fileId })"
            />

            <p-button
              styleClass="cursor-pointer"
              icon="fa-regular fa-trash-can"
              severity="danger"
              [outlined]="true"
              [disabled]="isActionRunning(file.fileId)"
              pTooltip="Delete file"
              tooltipPosition="top"
              showDelay="500"
              hideDelay="100"
              ariaLabel="Delete file"
              (click)="deleteSelected.emit(file.fileId)"
            />
          </div>
        </article>
      }
    </div>
  `
})
export class UploadedFilesList {
  readonly files = input.required<UploadedFileResponse[]>()
  readonly actionFileId = input<string | null>(null)
  readonly replacementSelected = output<{ file: File, fileId: string }>()
  readonly deleteSelected = output<string>()

  protected isActionRunning (fileId: string): boolean {
    return this.actionFileId() === fileId
  }

  protected readonly fileStatusOptions = computed(() =>
    Array.from(new Set(this.files().map((file) => file.fileStatus)))
      .sort()
      .map((status) => ({
        label: status,
        value: status
      }))
  )

  protected readonly tableFiles = computed(() =>
    this.files().map((file) => ({
      ...file,
      originalFilenameFilter: file.originalFilename.toLowerCase(),
      uploadedAtDate: this.toDateOnly(file.uploadedAt)
    }))
  )

  private toDateOnly (value: string): Date {
    const date = new Date(value)

    return new Date(date.getFullYear(), date.getMonth(), date.getDate())
  }
}
