import { Component, computed, input } from '@angular/core'

type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'success' | 'warning'
type ButtonType = 'button' | 'submit' | 'reset'

const baseClasses = [
  'inline-flex items-center justify-center rounded-md border px-3 py-2 text-sm font-medium transition',
  'focus:outline-none focus:ring-2 focus:ring-offset-2',
  'disabled:cursor-not-allowed disabled:opacity-60'
].join(' ')

const variantClasses: Record<ButtonVariant, string> = {
  primary: 'border-slate-900 bg-slate-900 text-white hover:bg-slate-800 focus:ring-slate-500',
  secondary: 'border-slate-300 bg-white text-slate-700 hover:border-slate-400 hover:bg-slate-50 focus:ring-slate-400',
  danger: 'border-rose-300 bg-white text-rose-700 hover:border-rose-400 hover:bg-rose-50 focus:ring-rose-400',
  success: 'border-emerald-700 bg-emerald-700 text-white hover:bg-emerald-600 focus:ring-emerald-500',
  warning: 'border-amber-300 bg-white text-amber-700 hover:border-amber-400 hover:bg-amber-50 focus:ring-amber-400'
}

@Component({
  selector: 'app-button',
  template: `
    <button
      [class]="classes()"
      [type]="type()"
      [disabled]="disabled() || loading()"
    >
      @if (loading()) {
        <span class="mr-2 size-3 animate-spin rounded-full border-2 border-current border-t-transparent"></span>
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

  protected readonly classes = computed(() => `${baseClasses} ${variantClasses[this.variant()]}`)
}
