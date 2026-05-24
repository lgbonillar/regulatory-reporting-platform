import { Component, computed, effect, ElementRef, HostListener, input, output, signal, viewChild } from '@angular/core'

import { AppButton } from '../app-button/app-button'

type ConfirmationVariant = 'primary' | 'secondary' | 'danger' | 'success' | 'warning'

@Component({
  selector: 'app-confirmation-dialog',
  imports: [ AppButton ],
  template: `
    @if (isOpen()) {
      <div
        class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/40 px-4 py-6"
        tabindex="-1"
        (mousedown)="cancelOnBackdrop($event)"
      >
        <section
          class="w-full max-w-md rounded-xl border border-slate-200 bg-white p-5 shadow-xl"
          role="dialog"
          aria-modal="true"
          [attr.aria-labelledby]="titleId"
        >
          <div class="flex flex-col gap-2">
            <h2 [id]="titleId" class="text-base font-semibold text-slate-950">
              {{ title() }}
            </h2>

            <p class="text-sm text-slate-600">
              {{ message() }}
            </p>
          </div>

          @if (requiresReason()) {
            <div class="mt-4">
              <label class="block text-sm font-medium text-slate-700" for="confirmation-reason">
                {{ reasonLabel() }}
              </label>

              <textarea
                #reasonTextarea
                id="confirmation-reason"
                class="mt-2 min-h-28 w-full resize-y rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-slate-500 focus:ring-2 focus:ring-slate-200 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-500"
                [placeholder]="reasonPlaceholder()"
                [disabled]="isSubmitting()"
                [value]="reason()"
                (input)="reason.set($any($event.target).value)"
              ></textarea>
            </div>
          }

          <div class="mt-5 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
            <app-button
              variant="secondary"
              [disabled]="isSubmitting()"
              (click)="cancel()"
            >
              {{ cancelLabel() }}
            </app-button>

            <app-button
              [variant]="confirmVariant()"
              [disabled]="isConfirmDisabled()"
              [loading]="isSubmitting()"
              (click)="submit()"
            >
              {{ confirmLabel() }}
            </app-button>
          </div>
        </section>
      </div>
    }
  `
})
export class ConfirmationDialog {

  readonly isOpen = input(false)
  readonly title = input.required<string>()
  readonly message = input.required<string>()
  readonly confirmLabel = input('Confirm')
  readonly cancelLabel = input('Cancel')
  readonly confirmVariant = input<ConfirmationVariant>('primary')
  readonly requiresReason = input(false)
  readonly reasonLabel = input('Reason')
  readonly reasonPlaceholder = input('')
  readonly isSubmitting = input(false)

  readonly cancelRequested = output<void>()
  readonly confirmRequested = output<string | null>()

  protected readonly reason = signal('')
  protected readonly reasonTextarea = viewChild<ElementRef<HTMLTextAreaElement>>('reasonTextarea')
  protected readonly titleId = `confirmation-dialog-title-${crypto.randomUUID()}`
  protected readonly isConfirmDisabled = computed(() =>
    this.isSubmitting() || (this.requiresReason() && !this.reason().trim())
  )

  constructor () {
    effect(() => {
      if (!this.isOpen()) {
        this.reason.set('')
        return
      }

      if (this.requiresReason()) {
        setTimeout(() => {
          const textarea = this.reasonTextarea()?.nativeElement

          textarea?.focus()
          textarea?.select()
        })
      }
    })
  }

  protected cancel (): void {
    if (this.isSubmitting()) return

    this.cancelRequested.emit()
  }

  protected cancelOnBackdrop (event: MouseEvent): void {
    if (event.target !== event.currentTarget) return

    this.cancel()
  }

  @HostListener('document:keydown.escape')
  protected cancelOnEscape (): void {
    if (!this.isOpen()) return

    this.cancel()
  }

  protected submit (): void {
    if (this.isConfirmDisabled()) return

    this.confirmRequested.emit(this.requiresReason() ? this.reason().trim() : null)
  }

}
