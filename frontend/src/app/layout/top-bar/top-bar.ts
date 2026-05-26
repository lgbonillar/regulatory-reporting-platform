import { Component, input, output } from '@angular/core'

import { CurrentUser } from '../../core/auth/session.model'

@Component({
  selector: 'app-top-bar',
  templateUrl: './top-bar.html'
})
export class TopBar {

  readonly activePageTitle = input.required<string>()
  readonly currentUser = input.required<CurrentUser>()
  readonly isSidebarCollapsed = input.required<boolean>()

  readonly logoutRequested = output<void>()

}
