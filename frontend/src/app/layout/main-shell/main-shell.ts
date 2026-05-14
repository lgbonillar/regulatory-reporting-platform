import { Component } from '@angular/core'
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router'

@Component({
  selector: 'app-main-shell',
  imports: [ RouterLink, RouterLinkActive, RouterOutlet ],
  templateUrl: './main-shell.html'
})
export class MainShell {
}
