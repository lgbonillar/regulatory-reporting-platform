import { Component, input, output } from '@angular/core'
import { RouterLink, RouterLinkActive } from '@angular/router'

import { NavigationItem } from '../../core/navigation/navigation.model'

@Component({
  selector: 'app-side-nav',
  imports: [ RouterLink, RouterLinkActive ],
  templateUrl: './side-nav.html'
})
export class SideNav {
  readonly items = input.required<readonly NavigationItem[]>()
  readonly isCollapsed = input.required<boolean>()

  readonly toggleCollapsed = output<void>()
  readonly logoutRequested = output<void>()

  protected getIconClass (item: NavigationItem): string {
    const icons: Record<string, string> = {
      files: 'fa-regular fa-folder-open',
      workflow: 'fa-solid fa-diagram-project',
      audit: 'fa-solid fa-clock-rotate-left'
    }

    return icons[item.icon] ?? 'fa-regular fa-circle'
  }
}
