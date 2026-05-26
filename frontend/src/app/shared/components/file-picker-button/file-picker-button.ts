import { Component, ElementRef, input, output, viewChild } from '@angular/core'
import { ButtonModule } from 'primeng/button'
import { TooltipModule } from 'primeng/tooltip'

@Component({
  selector: 'app-file-picker-button',
  imports: [ ButtonModule, TooltipModule ],
  template: `
      <input
        #fileInput
        class="hidden"
        type="file"
        accept=".xlsx"
        [disabled]="isUploading()"
        (change)="fileInputChanged($event)"
      />

      <p-button
        styleClass="cursor-pointer"
        icon="fa-solid fa-upload"
        severity="secondary"
        [outlined]="true"
        [loading]="isUploading()"
        [disabled]="isUploading()"
        ariaLabel="Update file"
        pTooltip="Update file"
        tooltipPosition="top"
        showDelay="500"
        hideDelay="100"
        (click)="openFilePicker()"
      />
  `
})
export class FilePickerButton {

  private readonly fileInput = viewChild.required<ElementRef<HTMLInputElement>>('fileInput')

  readonly accept = input('.xlsx')
  readonly disabled = input(false)
  readonly tooltip = input('Update file')
  readonly ariaLabel = input('Update file')
  readonly fileSelected = output<File>()
  readonly isUploading = input(false)

  protected openFilePicker (): void {
    if (this.isUploading()) return

    this.fileInput().nativeElement.click()
  }

  protected fileInputChanged (event: Event): void {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]

    input.value = ''

    if (!file) return

    this.fileSelected.emit(file)
  }

}
