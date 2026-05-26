import { HttpInterceptorFn } from '@angular/common/http'
import { inject } from '@angular/core'

import { AuthTokenStorageService } from './auth-token-storage.service'

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const tokenStorage = inject(AuthTokenStorageService)
  const accessToken = tokenStorage.getAccessToken()

  if (!accessToken || request.url.includes('/api/auth/login')) {
    return next(request)
  }

  return next(
    request.clone({
      setHeaders: {
        Authorization: `Bearer ${accessToken}`
      }
    })
  )
}
