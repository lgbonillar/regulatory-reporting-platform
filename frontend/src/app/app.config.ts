import { provideHttpClient } from '@angular/common/http'
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core'
import { provideRouter } from '@angular/router'
import { MessageService } from 'primeng/api'
import { providePrimeNG } from 'primeng/config'

import { routes } from './app.routes'
import { AppPrimeNgPreset } from './core/theme/app-primeng-theme'

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(),
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
