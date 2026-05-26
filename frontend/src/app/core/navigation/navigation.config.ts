import { NavigationItem } from './navigation.model'

export const NAVIGATION_ITEMS: readonly NavigationItem[] = [
  {
    label: 'Files',
    route: '/report-files/upload',
    icon: 'files',
    allowedRoles: [ 'ANALYST' ],
    pageTitle: 'Files'
  },
  {
    label: 'Processes',
    route: '/processing-jobs',
    icon: 'workflow',
    allowedRoles: [ 'ANALYST', 'ADMINISTRATOR' ],
    pageTitle: 'Processes'
  },
  {
    label: 'Events',
    route: '/events',
    icon: 'audit',
    allowedRoles: [ 'ADMINISTRATOR', 'AUDITOR' ],
    pageTitle: 'Events',
    disabled: true
  }
]
