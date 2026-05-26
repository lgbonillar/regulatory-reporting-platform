import { inject } from '@angular/core'
import { CanActivateFn, Router } from '@angular/router'

import { AuthService } from './auth.service'
import { SessionService } from './session.service'

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService)
  const sessionService = inject(SessionService)
  const router = inject(Router)

  authService.restoreSession()

  if (sessionService.isAuthenticated()) {
    return true
  }

  return router.createUrlTree([ '/login' ])
}
