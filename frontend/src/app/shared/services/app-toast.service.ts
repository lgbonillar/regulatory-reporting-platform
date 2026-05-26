import { inject, Injectable } from '@angular/core'
import { MessageService } from 'primeng/api'

type ToastSeverity = 'success' | 'info' | 'warn' | 'error'

const DEFAULT_TOAST_LIFE_MS = 3500

@Injectable({
  providedIn: 'root'
})
export class AppToastService {
  private readonly messageService = inject(MessageService)

  success (summary: string, detail?: string): void {
    this.show('success', summary, detail)
  }

  info (summary: string, detail?: string): void {
    this.show('info', summary, detail)
  }

  warning (summary: string, detail?: string): void {
    this.show('warn', summary, detail)
  }

  error (summary: string, detail?: string): void {
    this.show('error', summary, detail)
  }

  private show (severity: ToastSeverity, summary: string, detail?: string): void {
    this.messageService.add({
      severity,
      summary,
      detail,
      life: DEFAULT_TOAST_LIFE_MS
    })
  }
}
