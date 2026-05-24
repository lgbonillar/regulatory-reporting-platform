import { Component, input, output } from '@angular/core'

import { CurrentUser, UserRole } from '../../core/auth/session.model'

@Component({
  selector: 'app-top-bar',
  templateUrl: './top-bar.html'
})
export class TopBar {

  readonly activePageTitle = input.required<string>()
  readonly currentUser = input.required<CurrentUser>()
  readonly isSidebarCollapsed = input.required<boolean>()

  readonly mockRoleChanged = output<UserRole>()

  protected onRoleChange (event: Event): void {
    const select = event.target as HTMLSelectElement

    this.mockRoleChanged.emit(select.value as UserRole)
  }

}
