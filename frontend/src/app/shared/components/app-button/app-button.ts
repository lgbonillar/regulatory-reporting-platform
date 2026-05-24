import { Component, input } from '@angular/core'

type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'success' | 'warning'
type ButtonType = 'button' | 'submit' | 'reset'

@Component({
  selector: 'app-button',
  template: `
    <button
      class="inline-flex items-center justify-center rounded-md px-3 py-2 text-sm font-medium
transition
      focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:cursor-not-allowed
disabled:opacity-60"
      [class.bg-slate-900]="variant() === 'primary'"
      [class.text-white]="variant() === 'primary'"
      [class.hover:bg-slate-700]="variant() === 'primary'"
      [class.focus:ring-slate-500]="variant() === 'primary'"

      [class.border]="variant() === 'secondary'"
      [class.border-slate-300]="variant() === 'secondary'"
      [class.bg-white]="variant() === 'secondary'"
      [class.text-slate-700]="variant() === 'secondary'"
      [class.hover:bg-slate-50]="variant() === 'secondary'"
      [class.focus:ring-slate-500]="variant() === 'secondary'"

      [class.bg-red-600]="variant() === 'danger'"
      [class.text-white]="variant() === 'danger'"
      [class.hover:bg-red-500]="variant() === 'danger'"
      [class.focus:ring-red-500]="variant() === 'danger'"

      [class.bg-emerald-700]="variant() === 'success'"
      [class.text-white]="variant() === 'success'"
      [class.hover:bg-emerald-600]="variant() === 'success'"
      [class.focus:ring-emerald-500]="variant() === 'success'"

      [class.border]="variant() === 'warning'"
      [class.border-rose-300]="variant() === 'warning'"
      [class.bg-white]="variant() === 'warning'"
      [class.text-rose-700]="variant() === 'warning'"
      [class.hover:bg-rose-50]="variant() === 'warning'"
      [class.focus:ring-rose-500]="variant() === 'warning'"

      [type]="type()"
      [disabled]="disabled() || loading()"
    >
      @if (loading()) {
        <span class="mr-2 size-3 animate-spin rounded-full border-2 border-current border-t-
transparent"></span>
      }

      <ng-content />
    </button>
  `
})
export class AppButton {
  readonly variant = input<ButtonVariant>('secondary')
  readonly type = input<ButtonType>('button')
  readonly disabled = input(false)
  readonly loading = input(false)
}
