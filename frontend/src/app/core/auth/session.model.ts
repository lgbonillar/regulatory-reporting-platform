export type UserRole = 'ANALYST' | 'ADMINISTRATOR' | 'AUDITOR'

export interface CurrentUser {
  username: string
  displayName: string
  role: UserRole
}
