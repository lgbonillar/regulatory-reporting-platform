import { Component, input } from '@angular/core'
import { RouterLink } from '@angular/router'

export interface AppBreadcrumbItem {
  label: string
  route?: string
}

@Component({
  selector: 'app-breadcrumb',
  imports: [ RouterLink ],
  template: `
    <nav class="flex items-center gap-1 text-sm text-slate-500" aria-label="Breadcrumb">
      @for (item of items(); track item.label) {
        @if (item.route) {
          <a
            class="rounded-sm font-medium text-slate-600 underline decoration-slate-300 underline-offset-4 transition hover:text-slate-950 hover:decoration-slate-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-slate-300"
            [routerLink]="item.route"
          >
            {{ item.label }}
          </a>
        } @else {
          <span class="font-medium text-slate-900" aria-current="page">
            {{ item.label }}
          </span>
        }

        @if (!$last) {
          <span class="text-slate-400" aria-hidden="true">/</span>
        }
      }
    </nav>
  `
})
export class AppBreadcrumb {
  readonly items = input.required<readonly AppBreadcrumbItem[]>()
}
