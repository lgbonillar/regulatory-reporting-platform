import { Component, inject, signal } from '@angular/core'
import { RouterOutlet } from '@angular/router'

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

  protected readonly sessionService = inject(SessionService)
  protected readonly navigationService = inject(NavigationService)

  protected readonly isSidebarCollapsed = signal(false)

  protected toggleSidebar (): void {
    this.isSidebarCollapsed.update((value) => !value)
  }

}
