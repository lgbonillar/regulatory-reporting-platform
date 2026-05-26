import { inject } from '@angular/core'
import { CanActivateFn, Router } from '@angular/router'

import { UserRole } from './session.model'
import { SessionService } from './session.service'

export const roleGuard: CanActivateFn = (route) => {
  const sessionService = inject(SessionService)
  const router = inject(Router)
  const allowedRoles = route.data['roles'] as UserRole[] | undefined

  if (!allowedRoles?.length) {
    return true
  }

  const currentUser = sessionService.currentUser()

  if (currentUser && allowedRoles.includes(currentUser.role)) {
    return true
  }

  return router.createUrlTree([ '/forbidden' ])
}
