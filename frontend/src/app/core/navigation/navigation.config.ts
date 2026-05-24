import { NavigationItem } from './navigation.model'

export const NAVIGATION_ITEMS: readonly NavigationItem[] = [
  {
    label: 'Archivos',
    route: '/report-files/upload',
    icon: 'files',
    allowedRoles: [ 'ANALYST' ],
    pageTitle: 'Archivos'
  },
  {
    label: 'Procesos',
    route: '/processing-jobs',
    icon: 'workflow',
    allowedRoles: [ 'ANALYST', 'ADMINISTRATOR' ],
    pageTitle: 'Procesos'
  },
  {
    label: 'Eventos',
    route: '/events',
    icon: 'audit',
    allowedRoles: [ 'ADMINISTRATOR', 'AUDITOR' ],
    pageTitle: 'Eventos',
    disabled: true
  }
]
