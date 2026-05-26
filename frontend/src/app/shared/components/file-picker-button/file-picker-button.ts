import { Component, input, output } from '@angular/core'
import { FileUploadModule } from 'primeng/fileupload'

interface PrimeFileUploadSelectEvent {
  files?: File[]
}

@Component({
  selector: 'app-file-picker-button',
  imports: [ FileUploadModule ],
  template: `
    <p-fileupload
      mode="basic"
      name="file"
      chooseIcon="fa-solid fa-file-arrow-up"
      [chooseLabel]="label()"
      [accept]="accept()"
      [auto]="false"
      [customUpload]="true"
      [disabled]="disabled()"
      (onSelect)="fileSelected.emit($event)"
    />
  `
})
export class FilePickerButton {
  readonly label = input('Select file')
  readonly accept = input('.xlsx')
  readonly disabled = input(false)
  readonly fileSelected = output<PrimeFileUploadSelectEvent>()
}
