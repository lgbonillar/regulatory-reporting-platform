import { Component, inject } from '@angular/core'
import { MenuItem } from 'primeng/api'
import { ButtonModule } from 'primeng/button'
import { MenuModule } from 'primeng/menu'
import { TooltipModule } from 'primeng/tooltip'

import { AppToastService } from '../../services/app-toast.service'

@Component({
  selector: 'app-table-export-menu',
  imports: [ ButtonModule, MenuModule, TooltipModule ],
  template: `
    <p-menu #exportMenu appendTo="body" [model]="exportOptions" [popup]="true" />

    <p-button
      styleClass="cursor-pointer"
      icon="fa-solid fa-file-export"
      severity="secondary"
      [outlined]="true"
      pTooltip="Export"
      tooltipPosition="top"
      showDelay="500"
      hideDelay="100"
      ariaLabel="Export table"
      (click)="exportMenu.toggle($event)"
    />
  `
})
export class TableExportMenu {
  private readonly toast = inject(AppToastService)

  protected readonly exportOptions: MenuItem[] = [
    {
      label: 'XLSX',
      icon: 'fa-regular fa-file-excel',
      command: () => this.showExportPlaceholder('XLSX')
    },
    {
      label: 'PDF',
      icon: 'fa-regular fa-file-pdf',
      command: () => this.showExportPlaceholder('PDF')
    },
    {
      label: 'CSV',
      icon: 'fa-solid fa-file-csv',
      command: () => this.showExportPlaceholder('CSV')
    }
  ]

  private showExportPlaceholder (format: string): void {
    this.toast.info(`${format} export is not available yet`)
  }
}
