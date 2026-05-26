import { Component, inject, signal } from '@angular/core'
import { Router, RouterOutlet } from '@angular/router'

import { AuthService } from '../../core/auth/auth.service'
import { SessionService } from '../../core/auth/session.service'
import { NavigationService } from '../../core/navigation/navigation.service'
import { AppToastService } from '../../shared/services/app-toast.service'
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

  private readonly authService = inject(AuthService)
  private readonly router = inject(Router)
  private readonly toast = inject(AppToastService)

  protected readonly isSidebarCollapsed = signal(false)

  protected toggleSidebar (): void {
    this.isSidebarCollapsed.update((value) => !value)
  }

  protected logout (): void {
    this.authService.logout()
    this.toast.info('Session closed')
    void this.router.navigateByUrl('/login')
  }

}
