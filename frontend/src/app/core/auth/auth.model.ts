import { UserRole } from './session.model'

export interface LoginRequest {
  username: string
  password: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: 'Bearer'
  expiresInSeconds: number
}

export interface JwtAccessTokenPayload {
  sub: string
  userId: string
  username: string
  role: UserRole
  sessionId: string
  exp: number
  iat: number
  iss: string
}
