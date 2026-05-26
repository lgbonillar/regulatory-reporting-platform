import { inject } from '@angular/core'
import { CanActivateFn, Router } from '@angular/router'

import { NAVIGATION_ITEMS } from '../navigation/navigation.config'
import { SessionService } from './session.service'

export const defaultRouteGuard: CanActivateFn = () => {
  const sessionService = inject(SessionService)
  const router = inject(Router)
  const currentUser = sessionService.currentUser()

  if (!currentUser) {
    return router.createUrlTree([ '/login' ])
  }

  const defaultRoute = NAVIGATION_ITEMS.find((item) =>
    item.allowedRoles.includes(currentUser.role) && !item.disabled
  )?.route

  return router.createUrlTree([ defaultRoute ?? '/forbidden' ])
}
