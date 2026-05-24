import { Component, input } from '@angular/core'

type PageStateType = 'loading' | 'empty' | 'error'

@Component({
  selector: 'app-page-state',
  template: `
    <section
      class="rounded-lg border border-slate-200 bg-white px-6 py-10 text-center shadow-sm"
      [class.border-red-200]="type() === 'error'"
      [class.bg-red-50]="type() === 'error'"
    >
      <div class="mx-auto flex max-w-md flex-col items-center gap-3">
        <div
          class="flex size-10 items-center justify-center rounded-full"
          [class.bg-slate-100]="type() !== 'error'"
          [class.bg-red-100]="type() === 'error'"
        >
          @if (type() === 'loading') {
            <span class="size-4 animate-spin rounded-full border-2 border-slate-300 border-t-slate-700"></span>
          } @else if (type() === 'error') {
            <span class="text-sm font-semibold text-red-700">!</span>
          } @else {
            <span class="text-sm font-semibold text-slate-500">–</span>
          }
        </div>

        <div>
          <p
            class="text-sm font-semibold"
            [class.text-red-800]="type() === 'error'"
            [class.text-slate-900]="type() !== 'error'"
          >
            {{ title() }}
          </p>

          @if (message()) {
            <p
              class="mt-1 text-sm"
              [class.text-red-700]="type() === 'error'"
              [class.text-slate-500]="type() !== 'error'"
            >
              {{ message() }}
            </p>
          }
        </div>
      </div>
    </section>
  `
})
export class PageState {
  readonly type = input.required<PageStateType>()
  readonly title = input.required<string>()
  readonly message = input<string | null>(null)
}
