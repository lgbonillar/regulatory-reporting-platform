import { Injectable, signal } from '@angular/core'

import { CurrentUser } from './session.model'

@Injectable({
  providedIn: 'root'
})
export class SessionService {

  private readonly currentUserState = signal<CurrentUser>({
    username: 'analyst01',
    displayName: 'Analyst 01',
    role: 'ANALYST'
  })

  readonly currentUser = this.currentUserState.asReadonly()

  setMockRole (role: CurrentUser['role']): void {
    const usersByRole: Record<CurrentUser['role'], CurrentUser> = {
      ANALYST: {
        username: 'analyst01',
        displayName: 'Analyst 01',
        role: 'ANALYST'
      },
      ADMINISTRATOR: {
        username: 'admin01',
        displayName: 'Admin 01',
        role: 'ADMINISTRATOR'
      },
      AUDITOR: {
        username: 'auditor01',
        displayName: 'Auditor 01',
        role: 'AUDITOR'
      }
    }

    this.currentUserState.set(usersByRole[role])
  }

}
