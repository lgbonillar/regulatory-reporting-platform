import { Component, inject, signal } from '@angular/core'
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router'

import { UserRole } from '../../core/auth/session.model'
import { SessionService } from '../../core/auth/session.service'
import { NavigationItem } from '../../core/navigation/navigation.model'
import { NavigationService } from '../../core/navigation/navigation.service'

@Component({
  selector: 'app-main-shell',
  imports: [ RouterLink, RouterLinkActive, RouterOutlet ],
  templateUrl: './main-shell.html'
})
export class MainShell {
  protected readonly sessionService = inject(SessionService)
  protected readonly navigationService = inject(NavigationService)

  protected readonly isSidebarCollapsed = signal(false)

  protected toggleSidebar (): void {
    this.isSidebarCollapsed.update((value) => !value)
  }

  protected setMockRole (event: Event): void {
    const select = event.target as HTMLSelectElement

    this.sessionService.setMockRole(select.value as UserRole)
  }

  protected getIconLabel (item: NavigationItem): string {
    const labels: Record<string, string> = {
      files: 'F',
      workflow: 'P',
      audit: 'E'
    }

    return labels[item.icon] ?? '•'
  }
}
