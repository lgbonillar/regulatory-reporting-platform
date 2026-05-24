import { Component, input } from '@angular/core'

type PanelPadding = 'none' | 'normal'

@Component({
  selector: 'app-panel',
  template: `
    <section
      class="rounded-lg border border-slate-200 bg-white shadow-sm"
      [class.p-4]="padding() === 'normal'"
      [class.sm:p-6]="padding() === 'normal'"
    >
      <ng-content />
    </section>
  `
})
export class AppPanel {
  readonly padding = input<PanelPadding>('none')
}
