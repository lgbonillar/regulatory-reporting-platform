import { Component, inject, signal } from '@angular/core'
import { Router, RouterOutlet } from '@angular/router'

import { UserRole } from '../../core/auth/session.model'
import { SessionService } from '../../core/auth/session.service'
import { NavigationService } from '../../core/navigation/navigation.service'
import { SideNav } from '../side-nav/side-nav'
import { TopBar } from '../top-bar/top-bar'

@Component({
  selector: 'app-main-shell',
  imports: [ RouterOutlet, SideNav, TopBar ],
  templateUrl: './main-shell.html'
})
export class MainShell {
  private readonly router = inject(Router)

  protected readonly sessionService = inject(SessionService)
  protected readonly navigationService = inject(NavigationService)

  protected readonly isSidebarCollapsed = signal(false)

  protected toggleSidebar (): void {
    this.isSidebarCollapsed.update((value) => !value)
  }

  protected setMockRole (role: UserRole): void {
    this.sessionService.setMockRole(role)

    const firstAvailableItem = this.navigationService.navigationItems()
      .find((item) => !item.disabled)

    if (firstAvailableItem) {
      void this.router.navigateByUrl(firstAvailableItem.route)
    }
  }
}
