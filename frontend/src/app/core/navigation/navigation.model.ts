import { UserRole } from '../auth/session.model'

export interface NavigationItem {
  label: string
  route: string
  icon: string
  allowedRoles: readonly UserRole[]
  pageTitle: string
  disabled?: boolean
}
