import { computed, Injectable, signal } from '@angular/core'

import { JwtAccessTokenPayload } from './auth.model'
import { CurrentUser } from './session.model'

const MILLISECONDS_PER_SECOND = 1000

@Injectable({
  providedIn: 'root'
})
export class SessionService {
  private readonly currentUserState = signal<CurrentUser | null>(null)

  readonly currentUser = this.currentUserState.asReadonly()
  readonly isAuthenticated = computed(() => this.currentUserState() !== null)

  restoreFromAccessToken (accessToken: string | null): void {
    if (!accessToken || this.isTokenExpired(accessToken)) {
      this.clear()
      return
    }

    const payload = this.decodeAccessToken(accessToken)

    this.currentUserState.set({
      username: payload.username,
      displayName: payload.username,
      role: payload.role
    })
  }

  setAuthenticatedUser (accessToken: string): void {
    const payload = this.decodeAccessToken(accessToken)

    this.currentUserState.set({
      username: payload.username,
      displayName: payload.username,
      role: payload.role
    })
  }

  clear (): void {
    this.currentUserState.set(null)
  }

  private decodeAccessToken (accessToken: string): JwtAccessTokenPayload {
    const [ , payload ] = accessToken.split('.')
    const normalizedPayload = payload.replace(/-/g, '+').replace(/_/g, '/')
    const decodedPayload = atob(normalizedPayload)

    return JSON.parse(decodedPayload) as JwtAccessTokenPayload
  }

  private isTokenExpired (accessToken: string): boolean {
    const payload = this.decodeAccessToken(accessToken)
    const nowInSeconds = Math.floor(Date.now() / MILLISECONDS_PER_SECOND)

    return payload.exp <= nowInSeconds
  }
}
