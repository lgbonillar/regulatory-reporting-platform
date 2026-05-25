import { HttpClient } from '@angular/common/http'
import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core'

import { environment } from '../../../../environments/environments.dev'
import { FileStatus } from '../../../core/regulatory.model'

@Component({
  selector: 'app-file-download-link',
  template: `
    @if (isDownloadable()) {
      <button
        class="inline-flex max-w-full cursor-pointer items-start gap-1.5 text-left font-medium text-slate-950 underline decoration-slate-300 underline-offset-4 transition hover:text-emerald-700 hover:decoration-emerald-500 focus:outline-none focus-visible:ring-2 focus-visible:ring-emerald-200 disabled:cursor-not-allowed disabled:text-slate-400 disabled:decoration-slate-200"
        type="button"
        [disabled]="isDownloading()"
        [attr.aria-label]="'Download ' + filename()"
        (click)="downloadFile()"
      >
        <span class="break-words">
          {{ filename() }}
        </span>

        <svg
          class="mt-0.5 h-4 w-4 shrink-0"
          aria-hidden="true"
          viewBox="0 0 20 20"
          fill="currentColor"
        >
          <path
            fill-rule="evenodd"
            d="M10 2a.75.75 0 0 1 .75.75v7.69l2.22-2.22a.75.75 0 1 1 1.06 1.06l-3.5 3.5a.75.75 0 0 1-1.06 0l-3.5-3.5a.75.75 0 1 1 1.06-1.06l2.22 2.22V2.75A.75.75 0 0 1 10 2Zm-6.25 12a.75.75 0 0 1 .75.75v1.5h11v-1.5a.75.75 0 0 1 1.5 0V17a.75.75 0 0 1-.75.75H3.75A.75.75 0 0 1 3 17v-2.25a.75.75 0 0 1 .75-.75Z"
            clip-rule="evenodd"
          />
        </svg>
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
