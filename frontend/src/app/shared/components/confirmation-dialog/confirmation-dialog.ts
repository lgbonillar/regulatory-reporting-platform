import { Component, computed, effect, ElementRef, input, output, signal, viewChild } from '@angular/core'
import { DialogModule } from 'primeng/dialog'
import { TextareaModule } from 'primeng/textarea'

import { AppButton } from '../app-button/app-button'

type ConfirmationVariant = 'primary' | 'secondary' | 'danger' | 'success' | 'warning'

@Component({
  selector: 'app-confirmation-dialog',
  imports: [ AppButton, DialogModule, TextareaModule ],
  template: `
    <p-dialog
      styleClass="w-[calc(100vw-2rem)] max-w-md"
      [visible]="isOpen()"
      [modal]="true"
      [closable]="!isSubmitting()"
      [closeOnEscape]="!isSubmitting()"
      [draggable]="false"
      [resizable]="false"
      [header]="title()"
      (visibleChange)="onVisibleChange($event)"
    >
      <div class="flex flex-col gap-4">
        <p class="text-sm text-slate-600">
          {{ message() }}
        </p>

        @if (requiresReason()) {
          <div>
            <label class="block text-sm font-medium text-slate-700" for="confirmation-reason">
              {{ reasonLabel() }}
            </label>

            <textarea
              pTextarea
              #reasonTextarea
              id="confirmation-reason"
              class="mt-2 min-h-28 w-full resize-y text-sm"
              [placeholder]="reasonPlaceholder()"
              [disabled]="isSubmitting()"
              [value]="reason()"
              (input)="reason.set($any($event.target).value)"
            ></textarea>
          </div>
        }
      </div>

      <ng-template #footer>
        <div class="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
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
      </ng-template>
    </p-dialog>
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

  protected onVisibleChange (visible: boolean): void {
    if (visible || this.isSubmitting()) return

    this.cancelRequested.emit()
  }

  protected cancel (): void {
    if (this.isSubmitting()) return

    this.cancelRequested.emit()
  }

  protected submit (): void {
    if (this.isConfirmDisabled()) return

    this.confirmRequested.emit(this.requiresReason() ? this.reason().trim() : null)
  }

}
