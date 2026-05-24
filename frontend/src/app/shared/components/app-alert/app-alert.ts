import { Component, input } from '@angular/core'

type AlertType = 'success' | 'error' | 'info' | 'warning'

@Component({
  selector: 'app-alert',
  template: `
    <div
      class="rounded-md border px-4 py-3 text-sm"
      [class.border-emerald-200]="type() === 'success'"
      [class.bg-emerald-50]="type() === 'success'"
      [class.text-emerald-700]="type() === 'success'"
      [class.border-red-200]="type() === 'error'"
      [class.bg-red-50]="type() === 'error'"
      [class.text-red-700]="type() === 'error'"
      [class.border-sky-200]="type() === 'info'"
      [class.bg-sky-50]="type() === 'info'"
      [class.text-sky-700]="type() === 'info'"
      [class.border-amber-200]="type() === 'warning'"
      [class.bg-amber-50]="type() === 'warning'"
      [class.text-amber-700]="type() === 'warning'"
      role="status"
    >
      {{ message() }}
    </div>
  `
})
export class AppAlert {
  readonly type = input.required<AlertType>()
  readonly message = input.required<string>()
}
