import { HttpClient } from '@angular/common/http'
import { inject, Injectable } from '@angular/core'
import { Observable, tap } from 'rxjs'

import { environment } from '../../../environments/environments.dev'
import { ApiResponse } from '../../shared/utils/api-response'
import { AuthResponse, LoginRequest } from './auth.model'
import { AuthTokenStorageService } from './auth-token-storage.service'
import { SessionService } from './session.service'

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly httpClient = inject(HttpClient)
  private readonly tokenStorage = inject(AuthTokenStorageService)
  private readonly sessionService = inject(SessionService)

  private readonly endpointUrl = `${environment.apiBaseUrl}/api/auth`

  login (request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.httpClient
      .post<ApiResponse<AuthResponse>>(`${this.endpointUrl}/login`, request)
      .pipe(
        tap((response) => {
          this.tokenStorage.saveTokens(
            response.data.accessToken,
            response.data.refreshToken
          )
          this.sessionService.setAuthenticatedUser(response.data.accessToken)
        })
      )
  }

  logout (): void {
    this.tokenStorage.clear()
    this.sessionService.clear()
  }

  restoreSession (): void {
    this.sessionService.restoreFromAccessToken(
      this.tokenStorage.getAccessToken()
    )
  }
}
