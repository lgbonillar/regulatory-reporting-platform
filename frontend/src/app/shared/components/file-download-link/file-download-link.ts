import { HttpClient } from '@angular/common/http'
import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core'

import { environment } from '../../../../environments/environments.dev'
import { FileStatus } from '../../../core/regulatory.model'

@Component({
  selector: 'app-file-download-link',
  template: `
    @if (isDownloadable()) {
      <button
        class="break-words text-left font-medium text-slate-950 underline decoration-slate-300 cursor-pointer underline-offset-4 transition hover:text-emerald-700 hover:decoration-emerald-500 disabled:cursor-not-allowed disabled:text-slate-400 disabled:decoration-slate-200"
        type="button"
        [disabled]="isDownloading()"
        (click)="downloadFile()"
      >
        {{ filename() }}
      </button>
    } @else {
      <span class="wrap-break-words font-medium text-slate-500">
        {{ filename() }}
      </span>
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FileDownloadLink {

  private readonly httpClient = inject(HttpClient)

  readonly fileId = input.required<string>()
  readonly filename = input.required<string>()
  readonly fileStatus = input<FileStatus>('STORED')

  protected readonly isDownloadable = computed(() => this.fileStatus() === 'STORED')

  protected readonly isDownloading = signal(false)

  protected downloadFile (): void {
    this.isDownloading.set(true)

    this.httpClient
      .get(`${environment.apiBaseUrl}/api/report-files/${this.fileId()}/download`, {
        responseType: 'blob'
      })
      .subscribe({
        next: (blob) => {
          const objectUrl = URL.createObjectURL(blob)
          const anchor = document.createElement('a')

          anchor.href = objectUrl
          anchor.download = this.filename()
          anchor.click()

          URL.revokeObjectURL(objectUrl)
        },
        complete: () => {
          this.isDownloading.set(false)
        },
        error: () => {
          this.isDownloading.set(false)
        }
      })
  }
}
