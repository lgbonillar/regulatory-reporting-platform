import { HttpClient } from '@angular/common/http'
import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core'
import { TooltipModule } from 'primeng/tooltip'

import { environment } from '../../../../environments/environments.dev'
import { FileStatus } from '../../../core/regulatory.model'
import { AppToastService } from '../../services/app-toast.service'

@Component({
  selector: 'app-file-download-link',
  imports: [ TooltipModule ],
  template: `
    @if (isDownloadable()) {
      <button
        class="inline-flex max-w-full cursor-pointer items-start gap-1.5 text-left font-medium text-slate-950 underline decoration-slate-300 underline-offset-4 transition hover:text-emerald-700 hover:decoration-emerald-500 focus:outline-none focus-visible:ring-2 focus-visible:ring-emerald-200 disabled:cursor-wait disabled:text-slate-400 disabled:decoration-slate-200"
        type="button"
        pTooltip="Download file"
        tooltipPosition="top"
        showDelay="500"
        hideDelay="100"
        [disabled]="isDownloading()"
        [attr.aria-label]="'Download ' + filename()"
        (click)="downloadFile()"
      >
        <span class="break-words">
          {{ filename() }}
        </span>

        @if (isDownloading()) {
          <i class="fa-solid fa-circle-notch mt-0.5 h-4 w-4 shrink-0 animate-spin" aria-hidden="true"></i>
        } @else {
          <i class="fa-solid fa-download mt-0.5 h-4 w-4 shrink-0" aria-hidden="true"></i>
        }
      </button>
    } @else {
      <span
        class="wrap-break-words font-medium text-slate-500"
        pTooltip="File is not available for download"
        tooltipPosition="top"
        showDelay="500"
        hideDelay="100"
      >
        {{ filename() }}
      </span>
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FileDownloadLink {

  private readonly httpClient = inject(HttpClient)
  private readonly toast = inject(AppToastService)

  readonly fileId = input.required<string>()
  readonly filename = input.required<string>()
  readonly fileStatus = input<FileStatus>('STORED')

  protected readonly isDownloadable = computed(() =>
    this.fileStatus() === 'STORED' || this.fileStatus() === 'PENDING_CORRECTION'
  )
  protected readonly isDownloading = signal(false)

  protected downloadFile (): void {
    if (this.isDownloading()) return

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
          this.toast.success('Download started')
        },
        complete: () => {
          this.isDownloading.set(false)
        },
        error: () => {
          this.isDownloading.set(false)
          this.toast.error('Could not download file', this.filename())
        }
      })
  }
}
