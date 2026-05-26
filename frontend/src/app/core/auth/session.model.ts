export type UserRole = 'ROOT' | 'ADMINISTRATOR' | 'ANALYST' | 'AUDITOR'

export interface CurrentUser {
  username: string
  displayName: string
  role: UserRole
}
