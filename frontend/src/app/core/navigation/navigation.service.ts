import { computed, inject, Injectable, signal } from '@angular/core'
import { NavigationEnd, Router } from '@angular/router'
import { filter } from 'rxjs'

import { SessionService } from '../auth/session.service'
import { NAVIGATION_ITEMS } from './navigation.config'

@Injectable({
  providedIn: 'root'
})
export class NavigationService {
  private readonly router = inject(Router)
  private readonly sessionService = inject(SessionService)

  private readonly currentUrl = signal(this.router.url)

  readonly navigationItems = computed(() => {
    const currentRole = this.sessionService.currentUser().role

    return NAVIGATION_ITEMS.filter((item) => item.allowedRoles.includes(currentRole))
  })

  readonly activePageTitle = computed(() => {
    const activeItem = NAVIGATION_ITEMS.find((item) => this.currentUrl().startsWith(item.route))

    return activeItem?.pageTitle ?? 'Dashboard'
  })

  constructor () {
    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event) => {
        this.currentUrl.set(event.urlAfterRedirects)
      })
  }
}
