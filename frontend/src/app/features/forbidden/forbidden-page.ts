import { Component } from '@angular/core'
import { RouterLink } from '@angular/router'
import { ButtonModule } from 'primeng/button'

@Component({
  selector: 'app-forbidden-page',
  imports: [ ButtonModule, RouterLink ],
  template: `
    <main class="grid h-full place-items-center p-6">
      <section class="max-w-md rounded-xl border border-slate-200 bg-white p-6 text-center shadow-sm">
        <div class="mx-auto mb-4 flex size-12 items-center justify-center rounded-full bg-slate-100 text-slate-600">
          <i class="fa-solid fa-lock" aria-hidden="true"></i>
        </div>

        <h1 class="text-lg font-semibold text-slate-950">
          No available page
        </h1>

        <p class="mt-2 text-sm text-slate-500">
          Your role does not have an enabled page in this MVP yet.
        </p>

        <a
          pButton
          routerLink="/login"
          class="mt-5"
        >
          Back to login
        </a>
      </section>
    </main>
  `
})
export class ForbiddenPage {
}
