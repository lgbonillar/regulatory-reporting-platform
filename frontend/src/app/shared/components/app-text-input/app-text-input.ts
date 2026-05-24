import { Component, input, model } from '@angular/core'

type TextInputType = 'text' | 'email' | 'password' | 'search'

@Component({
  selector: 'app-text-input',
  template: `
    <input
      class="w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-700
outline-none transition
      placeholder:text-slate-400 focus:border-slate-500 focus:ring-2 focus:ring-slate-200
disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-500"
      [class.sm:w-64]="compact()"
      [type]="type()"
      [placeholder]="placeholder()"
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
  readonly disabled = input(false)
  readonly compact = input(false)
}
