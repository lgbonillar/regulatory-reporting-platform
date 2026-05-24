import { Component, input } from '@angular/core'

@Component({
  selector: 'app-page-header',
  template: `
    <header class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
      <div>
        <p class="text-sm font-medium text-slate-500">{{ eyebrow() }}</p>
        <h1 class="text-2xl font-semibold text-slate-950 sm:text-3xl">{{ title() }}</h1>
      </div>

      <ng-content />
    </header>
  `
})
export class PageHeader {
  readonly eyebrow = input.required<string>()
  readonly title = input.required<string>()
}
