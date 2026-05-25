import { Component, input } from '@angular/core'

type PanelPadding = 'none' | 'normal'

@Component({
  selector: 'app-panel',
  host: {
    class: 'block min-h-0',
    '[class.h-full]': 'fullHeight()',
    '[class.overflow-hidden]': 'fullHeight()'
  },
  template: `
    <section
      class="rounded-lg border border-slate-200 bg-white shadow-sm"
      [class.h-full]="fullHeight()"
      [class.min-h-0]="fullHeight()"
      [class.overflow-hidden]="fullHeight()"
      [class.p-4]="padding() === 'normal'"
      [class.sm:p-6]="padding() === 'normal'"
    >
      <ng-content />
    </section>
  `
})
export class AppPanel {

  readonly fullHeight = input(false)
  readonly padding = input<PanelPadding>('none')

}
