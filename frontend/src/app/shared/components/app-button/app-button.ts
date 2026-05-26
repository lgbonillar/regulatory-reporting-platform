import { Component, computed, input } from '@angular/core'
import { ButtonModule } from 'primeng/button'

type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'success' | 'warning'
type ButtonType = 'button' | 'submit' | 'reset'
type PrimeButtonSeverity = 'secondary' | 'success' | 'info' | 'warn' | 'danger' | 'help' |
'contrast'

@Component({
  selector: 'app-button',
  imports: [ ButtonModule ],
  template: `
    <p-button
      styleClass="cursor-pointer"
      [type]="type()"
      [severity]="severity()"
      [outlined]="outlined()"
      [loading]="loading()"
      [disabled]="disabled() || loading()"
    >
      <ng-content />
    </p-button>
  `
})
export class AppButton {
  readonly variant = input<ButtonVariant>('secondary')
  readonly type = input<ButtonType>('button')
  readonly disabled = input(false)
  readonly loading = input(false)

  protected readonly severity = computed<PrimeButtonSeverity | undefined>(() => {
    switch (this.variant()) {
    case 'primary':
      return undefined
    case 'secondary':
      return 'secondary'
    case 'danger':
      return 'danger'
    case 'success':
      return 'success'
    case 'warning':
      return 'warn'
    }
  })

  protected readonly outlined = computed(() => {
    return this.variant() === 'secondary' || this.variant() === 'danger' || this.variant() === 'warning'
  })
}
