import { Component, computed, input } from '@angular/core'
import { MessageModule } from 'primeng/message'

type AlertType = 'success' | 'info' | 'warn' | 'error'

type MessageSeverity = 'success' | 'info' | 'warn' | 'error'

@Component({
  selector: 'app-alert',
  imports: [ MessageModule ],
  template: `
    <p-message
      styleClass="w-full"
      [severity]="severity()"
    >
      {{ message() }}
    </p-message>
  `
})
export class AppAlert {
  readonly type = input<AlertType>('info')
  readonly message = input.required<string>()

  protected readonly severity = computed<MessageSeverity>(() => {
    if (this.type() === 'warn') return 'warn'

    return this.type()
  })
}
