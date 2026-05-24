import { Component, input, signal } from '@angular/core'

const COPIED_MESSAGE_DURATION = 1200

@Component({
  selector: 'app-copyable-code',
  template: `
    <span class="relative inline-flex max-w-full align-middle">
      <button
        class="file-text inline-flex max-w-full cursor-pointer items-center gap-1.5 rounded-md border border-slate-200 bg-slate-50 px-2 py-1 text-left text-xs font-medium text-slate-700 transition hover:border-slate-300 hover:bg-slate-100 focus:outline-none focus:ring-2 focus:ring-slate-300"
        type="button"
        [attr.aria-label]="ariaLabel()"
        (click)="copyValue()"
      >
        <code class="truncate text-xs">{{ value() }}</code>

        <svg
          class="size-3.5 shrink-0 text-slate-500"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          aria-hidden="true"
        >
          <rect x="9" y="9" width="13" height="13" rx="2" />
          <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
        </svg>
      </button>

      @if (isCopied()) {
        <span
          class="absolute -top-8 left-1/2 z-20 -translate-x-1/2 rounded-md bg-slate-900 px-2 py-1 text-xs font-medium text-white shadow-sm"
          role="status"
        >
          Copied!
        </span>
      }
    </span>
  `
})
export class CopyableCode {
  readonly value = input.required<string>()
  readonly ariaLabel = input('Copy value to clipboard')

  protected readonly isCopied = signal(false)
  private copiedMessageTimeout: ReturnType<typeof setTimeout> | null = null

  protected copyValue (): void {
    void this.writeToClipboard(this.value())
    this.showCopiedMessage()
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

  private showCopiedMessage (): void {
    this.isCopied.set(true)

    if (this.copiedMessageTimeout) {
      clearTimeout(this.copiedMessageTimeout)
    }

    this.copiedMessageTimeout = setTimeout(() => {
      this.isCopied.set(false)
      this.copiedMessageTimeout = null
    }, COPIED_MESSAGE_DURATION)
  }
}
