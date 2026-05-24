import { Component, computed, input, output } from '@angular/core'

const baseClasses = [
  'inline-flex items-center justify-center rounded-md border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 transition',
  'hover:bg-slate-50'
].join(' ')

@Component({
  selector: 'app-file-picker-button',
  template: `
    <label
      [class]="classes()"
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

  protected readonly classes = computed(() => {
    const stateClasses = this.disabled()
      ? 'cursor-not-allowed opacity-60'
      : 'cursor-pointer'

    return `${baseClasses} ${stateClasses}`
  })

  protected onFileSelected (event: Event): void {
    this.fileSelected.emit(event)
  }
}
