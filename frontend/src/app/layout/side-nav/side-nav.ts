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

  protected getIconLabel (item: NavigationItem): string {
    const labels: Record<string, string> = {
      files: 'F',
      workflow: 'P',
      audit: 'E'
    }

    return labels[item.icon] ?? '•'
  }
}
