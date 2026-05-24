import { HttpErrorResponse } from '@angular/common/http'

export function resolveHttpErrorMessage (error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    return error.error?.message ?? 'Request failed. Please try again.'
  }

  return 'Unexpected error. Please try again.'
}
