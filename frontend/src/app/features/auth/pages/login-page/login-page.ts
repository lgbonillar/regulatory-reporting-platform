import { Component, computed, inject, signal } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { Router } from '@angular/router'
import { ButtonModule } from 'primeng/button'
import { CardModule } from 'primeng/card'
import { InputTextModule } from 'primeng/inputtext'
import { PasswordModule } from 'primeng/password'

import { AuthService } from '../../../../core/auth/auth.service'
import { AppAlert } from '../../../../shared/components/app-alert/app-alert'
import { AppToastService } from '../../../../shared/services/app-toast.service'
import { resolveHttpErrorMessage } from '../../../../shared/utils/http-error-message'

@Component({
  selector: 'app-login-page',
  imports: [
    AppAlert,
    ButtonModule,
    CardModule,
    FormsModule,
    InputTextModule,
    PasswordModule
  ],
  template: `
    <main class="grid min-h-screen place-items-center bg-slate-100 px-4 py-10">
      <section class="w-full max-w-md">
        <div class="mb-6 text-center">
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">
            Regulatory reporting platform
          </p>
          <h1 class="mt-2 text-2xl font-semibold text-slate-950">
            Sign in
          </h1>
          <p class="mt-2 text-sm text-slate-500">
            Use your assigned credentials to access the platform.
          </p>
        </div>

        <p-card class="shadow-sm!">
          <form class="flex flex-col gap-4" (ngSubmit)="login()">
            @if (errorMessage()) {
              <app-alert type="error" [message]="errorMessage()!" />
            }

            <div class="flex flex-col gap-2">
              <label class="text-sm font-medium text-slate-700" for="username">
                Username
              </label>
              <input
                pInputText
                id="username"
                name="username"
                autocomplete="username"
                class="w-full"
                [disabled]="isSubmitting()"
                [ngModel]="username()"
                (ngModelChange)="username.set($event)"
              />
            </div>

            <div class="flex flex-col gap-2">
              <label class="text-sm font-medium text-slate-700" for="password">
                Password
              </label>
              <p-password
                inputId="password"
                name="password"
                autocomplete="current-password"
                class="w-full!"
                inputStyleClass="w-full"
                [feedback]="false"
                [toggleMask]="true"
                [disabled]="isSubmitting()"
                [ngModel]="password()"
                (ngModelChange)="password.set($event)"
              />
            </div>

            <p-button
              type="submit"
              label="Sign in"
              styleClass="w-full"
              [loading]="isSubmitting()"
              [disabled]="isSubmitDisabled()"
            />

            <div class="rounded-lg bg-slate-50 p-3 text-sm text-slate-500">
              <p class="font-medium text-slate-700">Demo users</p>
              <p class="mt-1 file-text">analyst01 / password</p>
              <p class="file-text">admin01 / password</p>
              <p class="file-text">auditor01 / password</p>
              <p class="file-text">root01 / password</p>
            </div>
          </form>
        </p-card>
      </section>
    </main>
  `
})
export class LoginPage {
  private readonly authService = inject(AuthService)
  private readonly router = inject(Router)
  private readonly toast = inject(AppToastService)

  protected readonly username = signal('analyst01')
  protected readonly password = signal('password')
  protected readonly errorMessage = signal<string | null>(null)
  protected readonly isSubmitting = signal(false)

  protected readonly isSubmitDisabled = computed(() =>
    this.isSubmitting() ||
    !this.username().trim() ||
    !this.password().trim()
  )

  protected login (): void {
    if (this.isSubmitDisabled()) return

    this.isSubmitting.set(true)
    this.errorMessage.set(null)

    this.authService.login({
      username: this.username().trim(),
      password: this.password()
    }).subscribe({
      next: () => {
        this.toast.success('Login successful')
        void this.router.navigateByUrl('/')
      },
      error: (error: unknown) => {
        const message = resolveHttpErrorMessage(error)

        this.errorMessage.set(message)
        this.toast.error('Could not sign in', message)
        this.isSubmitting.set(false)
      },
      complete: () => {
        this.isSubmitting.set(false)
      }
    })
  }
}
