import { Component, computed, input, model } from '@angular/core'
import { InputTextModule } from 'primeng/inputtext'

type TextInputType = 'text' | 'email' | 'password' | 'search' | 'date'

@Component({
  selector: 'app-text-input',
  imports: [ InputTextModule ],
  template: `
    <input
      pInputText
      [class]="classes()"
      [type]="type()"
      [placeholder]="placeholder()"
      [attr.aria-label]="ariaLabel()"
      [disabled]="disabled()"
      [value]="value()"
      (input)="value.set($any($event.target).value)"
    />
  `
})
export class AppTextInput {
  readonly value = model('')
  readonly type = input<TextInputType>('text')
  readonly placeholder = input('')
  readonly ariaLabel = input<string | null>(null)
  readonly disabled = input(false)
  readonly compact = input(false)

  protected readonly classes = computed(() => {
    const widthClass = this.compact() ? 'w-full sm:w-64' : 'w-full'

    return `${widthClass} text-sm`
  })
}
