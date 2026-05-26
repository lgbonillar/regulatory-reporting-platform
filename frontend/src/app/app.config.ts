import { provideHttpClient, withInterceptors } from '@angular/common/http'
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core'
import { provideRouter } from '@angular/router'
import { MessageService } from 'primeng/api'
import { providePrimeNG } from 'primeng/config'

import { routes } from './app.routes'
import { authInterceptor } from './core/auth/auth.interceptor'
import { AppPrimeNgPreset } from './core/theme/app-primeng-theme'

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(withInterceptors([ authInterceptor ])),
    provideRouter(routes),
    providePrimeNG({
      ripple: false,
      theme: {
        preset: AppPrimeNgPreset,
        options: {
          darkModeSelector: false
        }
      }
    }),
    MessageService
  ]
}
