import { Component, computed, input } from '@angular/core'
import { ProgressSpinnerModule } from 'primeng/progressspinner'

type PageStateType = 'loading' | 'empty' | 'error'

@Component({
  selector: 'app-page-state',
  imports: [ ProgressSpinnerModule ],
  template: `
    <section
      class="rounded-lg border px-6 py-10 text-center shadow-sm"
      [class]="containerClasses()"
    >
      <div class="mx-auto flex max-w-md flex-col items-center gap-3">
        <div
          class="flex size-11 items-center justify-center rounded-full"
          [class]="iconShellClasses()"
        >
          @if (type() === 'loading') {
            <p-progress-spinner
              ariaLabel="Loading"
              class="size-6!"
              strokeWidth="6"
            />
          } @else if (type() === 'error') {
            <i class="fa-solid fa-triangle-exclamation text-base"></i>
          } @else {
            <i class="fa-regular fa-folder-open text-base"></i>
          }
        </div>

        <div>
          <p
            class="text-sm font-semibold"
            [class]="titleClasses()"
          >
            {{ title() }}
          </p>

          @if (message()) {
            <p
              class="mt-1 text-sm"
              [class]="messageClasses()"
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

  protected readonly containerClasses = computed(() => {
    if (this.type() === 'error') return 'border-red-200 bg-red-50'

    return 'border-slate-200 bg-white'
  })

  protected readonly iconShellClasses = computed(() => {
    if (this.type() === 'error') return 'bg-red-100 text-red-700'

    return 'bg-slate-100 text-slate-500'
  })

  protected readonly titleClasses = computed(() => {
    if (this.type() === 'error') return 'text-red-800'

    return 'text-slate-900'
  })

  protected readonly messageClasses = computed(() => {
    if (this.type() === 'error') return 'text-red-700'

    return 'text-slate-500'
  })
}
