import { HttpErrorResponse } from '@angular/common/http'

export function resolveHttpErrorMessage (error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    const envelope = error.error

    if (envelope && typeof envelope === 'object' && 'message' in envelope) {
      return (envelope as { message: string }).message ?? 'Request failed. Please try again.'
    }

    if (typeof envelope === 'string' && envelope.trim().length > 0) {
      return envelope
    }

    if (error.status === 0) {
      return 'Unable to connect to the server. Please check your network connection.'
    }

    return `Request failed (${error.status}). Please try again.`
  }

  return 'Unexpected error. Please try again.'
}
