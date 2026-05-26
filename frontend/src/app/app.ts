import { ChangeDetectionStrategy, Component } from '@angular/core'
import { ToastModule } from 'primeng/toast'

import { MainShell } from './layout/main-shell/main-shell'

@Component({
  selector: 'app-root',
  imports: [ MainShell, ToastModule ],
  templateUrl: './app.html',
  styleUrl: './app.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class App {
}
