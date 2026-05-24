import { computed, inject, Injectable } from '@angular/core'
import { NavigationEnd, Router } from '@angular/router'
import { filter, startWith } from 'rxjs'

import { SessionService } from '../auth/session.service'
import { NAVIGATION_ITEMS } from './navigation.config'

@Injectable({
  providedIn: 'root'
})
export class NavigationService {
  private readonly router = inject(Router)
  private readonly sessionService = inject(SessionService)

  readonly navigationItems = computed(() => {
    const currentRole = this.sessionService.currentUser().role

    return NAVIGATION_ITEMS.filter((item) => item.allowedRoles.includes(currentRole))
  })

  readonly activePageTitle = computed(() => {
    const currentUrl = this.router.url
    const activeItem = NAVIGATION_ITEMS.find((item) => currentUrl.startsWith(item.route))

    return activeItem?.pageTitle ?? 'Dashboard'
  })

  constructor () {
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        startWith(null)
      )
      .subscribe(() => {
        this.activePageTitle()
      })
  }
}
