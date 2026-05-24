import { Component, input, output } from '@angular/core'

@Component({
  selector: 'app-file-picker-button',
  template: `
    <label
      class="inline-flex cursor-pointer items-center justify-center rounded-md border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-50"
      [class.pointer-events-none]="disabled()"
      [class.opacity-50]="disabled()"
    >
      {{ label() }}

      <input
        class="sr-only"
        type="file"
        [accept]="accept()"
        [disabled]="disabled()"
        (change)="onFileSelected($event)"
      />
    </label>
  `
})
export class FilePickerButton {
  readonly label = input('Select file')
  readonly accept = input('.xlsx')
  readonly disabled = input(false)
  readonly fileSelected = output<Event>()

  protected onFileSelected (event: Event): void {
    this.fileSelected.emit(event)
  }
}
