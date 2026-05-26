import { Component, inject, input } from '@angular/core'

import { AppToastService } from '../../services/app-toast.service'

@Component({
  selector: 'app-copyable-code',
  template: `
    <span class="inline-flex max-w-full align-middle">
      <button
        class="file-text inline-flex max-w-full cursor-pointer items-center gap-1.5 rounded-md border border-slate-200 bg-slate-50 px-2 py-1 text-left text-xs font-medium text-slate-700 transition hover:border-slate-300 hover:bg-slate-100 focus:outline-none focus:ring-2 focus:ring-slate-300"
        type="button"
        [attr.aria-label]="ariaLabel()"
        (click)="copyValue()"
      >
        <code class="truncate text-xs">{{ value() }}</code>

        <i class="fa-regular fa-copy size-3.5 shrink-0 text-slate-500" aria-hidden="true"></i>
      </button>
    </span>
  `
})
export class CopyableCode {
  readonly value = input.required<string>()
  readonly ariaLabel = input('Copy value to clipboard')

  private readonly toast = inject(AppToastService)

  protected copyValue (): void {
    void this.writeToClipboard(this.value())
      .then(() => this.toast.success('Copied!'))
      .catch(() => this.toast.error('Could not copy value'))
  }

  private async writeToClipboard (value: string): Promise<void> {
    if (navigator.clipboard) {
      await navigator.clipboard.writeText(value)
      return
    }

    const textarea = document.createElement('textarea')

    textarea.value = value
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
  }
}
